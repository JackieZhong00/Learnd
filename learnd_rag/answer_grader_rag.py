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
# from dotenv import load_dotenv
# load_dotenv()



os.environ['LANGCHAIN_TRACING_V2'] = 'true'
os.environ['LANGSMITH_PROJECT'] = "rag_microservice"
os.environ['LANGCHAIN_ENDPOINT'] = "https://api.smith.langchain.com"

class AnswerGraphState(BaseModel):
    userId : int 
    cardId : int
    category: str
    question: str
    answer: str 
    grade: int


def web_search(state):
    searchPrompt = """
    Act as an expert in {state.category} to find as much relevant, factual,
    trustworthy information as possible to answer
    the question: {state.question}"""

    tavily_client = TavilyClient(api_key=os.getenv("TAVILY_API_KEY"))
    response = tavily_client.search(searchPrompt)
    state.facts = response['answer']
    return state

def index_and_grade(state):
    connection = psycopg2.connect(
        dbname="postgres",
        user = os.getenv("postgres_user"),
        password = os.getenv("postgres_password"),
        host="localhost",
        port=3030,
    )
    cur = connection.cursor()

    textsplitter = RecursiveCharacterTextSplitter(
        chunk_size = 1000,
        chunk_overlap = 200,
    )
    splits = textsplitter.split_text(state.facts)
    embeddingObject = OpenAIEmbeddings(model="text-embedding-3-small")
    embeddings = embeddingObject.embed_documents(splits)
    for e in embeddings:
        cur.execute(f"INSERT INTO grading_embeddings (userid, cardid, embedding) VALUES ({state.userId}, {state.cardId}, {e})  ")



    grading_prompt = textwrap.dedent("""You are an professional educator and industry expert in {category}.
        Given the question: {question}
        , the provided answer: {answer}
        , and the following relevant information: {facts}
        , grade how accurate the answer is in ansewering the question
        on a scale of 0 to 100 where 0 is completely inaccurate and 100 is completely accurate.""")

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
    
    cur.close()
    connection.commit()
    connection.close()
    return state



def compile_answer_grader(answered_card):
    workflow = StateGraph(AnswerGraphState)
    workflow.add_node("web_search", web_search)
    workflow.add_node("index_and_grade", index_and_grade)
    workflow.add_edge(START, "web_search")
    workflow.add_edge("web_search", "index_and_grade")
    workflow.add_edge("index_and_grade", END)

    state_to_run = AnswerGraphState(
        userId = answered_card.user_id,
        cardId = answered_card.card_id,
        category = answered_card.category,
        question = answered_card.messages[0].text,
        answer = answered_card.messages[1].text,
        grade = 0
    )
    app = workflow.run(state_to_run)

    return app.grade
 
