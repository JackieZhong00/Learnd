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
    # template = """
    #   You are an working professional who teaches this specific topic: {category}. 
    #   Using related facts from reliable web sources related to this topic, such as {facts},
    #   grade how correct the following answer is to the question asked.
    #   The question asked: {question}. The answer to grade: {answer}.
    #   Grade the answer on a scale of 0 to 100 where 0 is completely incorrect
    #   and 100 is the perfect answer. Provide only the numeric grade as output.

    # """
    # prompt = ChatPromptTemplate.from_template(template)
    # generate_queries = (
    # prompt
    # | ChatOpenAI(model="gpt-3.5-turbo",temperature=0.7)
    # | StrOutputParser()
    # )
    
    # response = generate_queries.invoke({
    #     category: state.category,
    #     question: state.question,
    #     answer: state.answer,     
    # })
    searchPrompt = """
    Act as an expert in {state.category} to find as much relevant, factual,
    trustworthy information as possible to answer
    the question: {state.question}"""

    tavily_client = TavilyClient(api_key=os.getenv("TAVILY_API_KEY"))
    response = tavily_client.search(searchPrompt)
    state.facts = response['answer']
    return "index_documents"

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

    #need to split and embed the response stored in state.facts 
    #then need to figure out how to store into embeddings table
    #not sure if i need to provide a specific primary key for the insert 
    #statement or if the autogenerate setting for the table is set already 

    # cur.execute("INSERT INTO embeddings ")


    #then grade the answer by pulling the most useful information
    #from the indexed documents 
    
    return END



def compile_answer_grader(answered_card):
    workflow = StateGraph(AnswerGraphState)
    workflow.add_node("web_search", web_search)
    workflow.add_node("index_documents", index_documents)
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

    return 
