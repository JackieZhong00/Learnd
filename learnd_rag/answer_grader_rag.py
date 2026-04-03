import psycopg2
import os
from langgraph.graph import END, StateGraph, START
from langchain_core.retrievers import BaseRetriever
from langchain.schema import Document
from typing import List
from typing_extensions import TypedDict
from langchain_openai import OpenAIEmbeddings
from langchain_chroma import Chroma
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_openai import ChatOpenAI
from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain.output_parsers import PydanticOutputParser
from pydantic import BaseModel
from tavily import TavilyClient
import textwrap
from dotenv import load_dotenv
from pgvector.psycopg2 import register_vector
load_dotenv(override=True)




os.environ['LANGCHAIN_TRACING_V2'] = 'true'
os.environ['LANGSMITH_PROJECT'] = "rag_microservice"
os.environ['LANGCHAIN_ENDPOINT'] = "https://api.smith.langchain.com"

class AnswerGraphState(BaseModel):
    userId : int 
    category: str
    question: str
    answer: str 
    grade: int
    warmup: bool
    needsIndexing: bool
    flashcardId: int 
    facts: str

tavily_client = TavilyClient(api_key=os.getenv("TAVILY_API_KEY"))

def web_search(state):
    print("entered websearch node \n")
    if state.needsIndexing == False: return state 
    if state.warmup: return state
    searchPrompt = f"""
    Act as an expert in {state.category} to find as much relevant, factual,
    trustworthy information as possible to answer
    the question: {state.question}"""
    print("search prompt: ", searchPrompt)
    response = tavily_client.search(searchPrompt)
    resultsList = response['results']
    print("resultsList: ", resultsList)
    topThreeResults = []
    try: 
        print("processing tavily response \n")
        topThreeResults = [
            result['content'] for result in sorted(resultsList, key=lambda x: x['score'], reverse=True)[:3]
        ]
        print("topThreeResults processed \n")
    except Exception as e:
        print("Error processing tavily response: ", e)
        raise e 
    print("topThreeResults: ", topThreeResults)
    state.facts = "".join(topThreeResults)
    print("state.facts: ", state.facts)
    return state

def gradeOnly(state):
    print("entered gradeOnly")
    connection = None
    try: 
        connection = psycopg2.connect(
        dbname=os.getenv("postgres_dbname"),
        user = os.getenv("postgres_user"),
        password = os.getenv("postgres_password"),
        host="localhost",
        port=5431,
        )
    except Exception as e:
        print("Error connecting to database: ", e)
        raise e
    register_vector(connection)
    cur = connection.cursor()
    #retrieve relevant context
    print("successfully connected to postgres")
    embeddingObject = OpenAIEmbeddings(model="text-embedding-3-small")
    #question_embedding is type list 
    question_embedding = embeddingObject.embed_query(state.question)
    print("question_embedding is of this type: ", type(question_embedding))
    try:
        embedding_str = "[" + ",".join(str(x) for x in question_embedding) + "]"
        cur.execute("SELECT content FROM grading_embeddings ORDER BY embedding <=> %s LIMIT 3;", (embedding_str,))
        retrievedContent = [result [0] for result in cur.fetchall()]
    except Exception as e:
        print("error with retrieving context from grading_embeddings: " , e)
        raise e
    print("retrived context arr: ", retrievedContent)
    try:
        # print("retrievedContextArr is of type: ", type(retrievedContextArr)) 
        state.facts = "".join(retrievedContent)
    except Exception as e:
        print("Error storing retrieved context into state.facts: ", e)
        raise e


    grading_prompt = textwrap.dedent("""You are an professional educator and industry expert in {category}.
        Given the question: {question}
        , the provided answer: {answer}
        , and the following relevant information: {facts}
        , grade how accurate the answer is in ansewering the question
        on a scale of 0 to 100 where 0 is completely inaccurate and 100 is completely accurate. 
        Only generate a number as output. Do not include any words in output.""")

    finalPrompt = ChatPromptTemplate.from_template(grading_prompt)    
    llm = ChatOpenAI(model ="gpt-3.5-turbo", temperature=0.7)
    grader_chain = (
        finalPrompt
        | llm
        | StrOutputParser()
    )
    result = grader_chain.invoke({
        "category": state.category,
        "question": state.question,
        "answer": state.answer,
        "facts": state.facts})

    state.grade = int(result)
    print("got to this line in gradeOnly")
    cur.close()
    connection.commit()
    connection.close()
    return state

def index_and_grade(state):
    print("entered index and grade node \n")
    if state.warmup: return state 
    if state.needsIndexing == False: 
        return gradeOnly(state)
    connection = None
    try: 
        connection = psycopg2.connect(
        dbname=os.getenv("postgres_dbname"),
        user = os.getenv("postgres_user"),
        password = os.getenv("postgres_password"),
        host="localhost",
        port=5431,
        )
    except Exception as e:
        print("Error connecting to database: ", e)
        raise e
    
    register_vector(connection)
    cur = connection.cursor()
    print("connected to db for indexing \n")

    #split tavily results and create embeddings to store in db
    textsplitter = RecursiveCharacterTextSplitter(
        chunk_size = 1000,
        chunk_overlap = 200,
    )
    splits = textsplitter.split_text(state.facts)
    print("created text splits for indexing \n")
    embeddingObject = OpenAIEmbeddings(model="text-embedding-3-small")
    print("created embedding object for indexing \n")
    embeddings = embeddingObject.embed_documents(splits)
    print("length of splits: ", len(splits), "\n")
    print("length of embeddings: ", len(embeddings), "\n")
    print("created embeddings for indexing \n")
    for i in range(len(embeddings)):
        # print("inserting embedding ", embeddings[i], " into db with the split: ", splits[i], "\n")
        print("length of embedding: ", len(embeddings[i]), "\n")
        try:
            cur.execute("INSERT INTO grading_embeddings (userid, content, embedding) values (%s, %s, %s)", (state.userId, splits[i], embeddings[i]))
        except Exception as e:
            print("Error inserting embedding into database: ", e)
            raise e


    print("inserted embeddings into db for indexing \n")
    #close connection used for indexing since gradeOnly() will open a new one
    cur.close()
    connection.commit()
    connection.close()

    #now grade the answer
    state = gradeOnly(state)
    
    
    return state

workflow = StateGraph(AnswerGraphState)
workflow.add_node("web_search", web_search)
workflow.add_node("index_and_grade", index_and_grade)
workflow.add_edge(START, "web_search")
workflow.add_edge("web_search", "index_and_grade")
workflow.add_edge("index_and_grade", END)
app = workflow.compile()

def compile_answer_grader(answered_card):
    print("entered compile answer grader \n")
    try: 
        connection = psycopg2.connect(
        dbname=os.getenv("postgres_dbname"),
        user = os.getenv("postgres_user"),
        password = os.getenv("postgres_password"),
        host="localhost",
        port=5431,
        )
    except Exception as e:
        print("Error connecting to database: ", e)
        raise e
    cur = connection.cursor()
    #update the index status of card
    cur.execute("UPDATE rag_index_state_logs SET status = 'INDEXING' WHERE flashcard_id = %s AND status = 'NOT_INDEXED' RETURNING flashcard_id;",(answered_card.flashcardId,))
    # cur.execute("INSERT INTO rag_index_state_logs (flashcard_id, status) values (1000, 'INDEXING')")
    result = cur.fetchone()
    print("result: ", result)
    cur.close()
    connection.commit()
    connection.close()
    state_to_run = AnswerGraphState(
        userId = answered_card.userId,
        category = answered_card.category,
        question = answered_card.question,
        answer = answered_card.answer,
        grade = 0,
        warmup = False,
        needsIndexing = True,
        flashcardId = answered_card.flashcardId,
        facts = ""
    )
    if not result:
        print("not indexing because nothing was fetched from db")
        state_to_run.needsIndexing = False
    print("State to run: \n", state_to_run)
    result = app.invoke(state_to_run)
    print("result: ", result)
    return result["grade"]
 
