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
from dotenv import load_dotenv
load_dotenv()

os.environ['LANGCHAIN_TRACING_V2'] = 'true'
os.environ['LANGCHAIN_ENDPOINT'] = os.getenv('LANGCHAIN_ENDPOINT')
os.environ['LANGCHAIN_API_KEY'] = os.getenv('LANGCHAIN_API_KEY')
os.environ['LANGSMITH_PROJECT'] = "rag_microservice"
os.environ['OPENAI_API_KEY'] = os.getenv('OPENAI_API_KEY')
def get_cluster_with_deck_id(deck_id):
    connection = psycopg2.connect(
    dbname=os.getenv("postgres_dbname"),
    user=os.getenv("postgres_user"),
    password=os.getenv("postgres_password"),
    host=os.getenv("postgres_host"),
    port=os.getenv("postgres_port"),
    )
    cur = connection.cursor()

    #comma makes it a tuple, which is required for sql injection protection
    cur.execute("SELECT cluster FROM Deck WHERE id = %s;", (deck_id,))
    result = cur.fetchone()
    print("Retrieved cluster for deck_id", deck_id, ":", result)
    cluster = result[0] if result else None
    print("Cluster summary:", cluster)
    cur.close()
    return cluster

def store_cluster_with_deck_id(cluster_string, deck_id):
    connection = psycopg2.connect(
    dbname=os.getenv("postgres_dbname"),
    user=os.getenv("postgres_user"),
    password=os.getenv("postgres_password"),
    host=os.getenv("postgres_host"),
    port=os.getenv("postgres_port")
)
    cur = connection.cursor()
    cur.execute("UPDATE deck SET cluster = %s WHERE id = %s;", (cluster_string, deck_id))
    connection.commit()
    cur.close()
    connection.close()
    print("successfully stored cluster")



def format_flashcard(card_event):
    text = f"""Q: {card_event.question}
A: {card_event.answer}
"""
    metadata = {
        "cardId": card_event.cardId,
        "deckId": card_event.deckId,
        "userId": card_event.userId,
        "isMultipleChoice": card_event.isMultipleChoice,
        "question": card_event.question,
        "answer": card_event.answer,
    }
    return Document(page_content=text, metadata=metadata)


def format_feedback(feedback_event):
    text = f"""Q: {feedback_event.question}
A: {feedback_event.answer}
"""
    metadata = {
        "wasAccepted": feedback_event.wasAccepted,
        "userId": feedback_event.userId,
        "deckId": feedback_event.deckId,
        "question": feedback_event.question,
        "answer": feedback_event.answer,
        "isMultipleChoice": feedback_event.isMultipleChoice,
    }    
    return Document(page_content=text, metadata=metadata)

def embed_cards(created_cards, deleted_cards_ids, updated_cards, updated_cards_ids, feedback):
    #embed cards
    vectorstore = Chroma(persist_directory="./chroma_db", embedding_function=OpenAIEmbeddings())
    if(len(created_cards) != 0):
        vectorstore.add_documents(created_cards, ids=[str(doc.metadata['cardId']) for doc in created_cards])


    if(len(deleted_cards_ids) != 0):
        vectorstore.delete(deleted_cards_ids)
    if(len(updated_cards) != 0):
        for idx, cardId in enumerate(updated_cards_ids):
            vectorstore.delete(cardId)
            vectorstore.add_documents([updated_cards[idx]],ids=[str(updated_cards[idx].metadata['cardId'])])


    if(len(feedback) != 0):
        vectorstore.add_documents(feedback, metadatas=[{"userId": doc.metadata['userId']} for doc in feedback])


    return vectorstore

def generate_cluster(created_cards, deleted_cards, updated_cards, categories, deck_name, deck_id, feedback):
    #ask llm to generate new clusters given these new card updates - just pass in batch_message and old cluster as context
    print("Start of generating cluster function")
    template = """Generate a new summary representing precisely the overarching, higher level concepts and lower level nuances that the user understands,based off of the
      most recent summary of the user's knowledge: {summary}
    , the most recent flashcards added to the deck: {create_cards}
    , the most recent flashcard updates made to the deck's flashcards: {updated_cards}
    , and the most recent flashcards removed from the deck: {deleted_cards}, where the deck name is {deck_name} and the categories that the deck is placed under are {categories}.
    The new summary should include an evaluation of what the user knows so far and what the user does not want to include based on feedback: {feedback}. 
    """
    prompt = ChatPromptTemplate.from_template(template)
    generate_queries = (
    prompt
    | ChatOpenAI(model="gpt-3.5-turbo",temperature=0.7)
    | StrOutputParser()
    )
    prompt_arg = {
        "summary": get_cluster_with_deck_id(deck_id),
        "create_cards": created_cards,
        "updated_cards": updated_cards,
        "deleted_cards":deleted_cards ,
        "deck_name": deck_name,
        "categories": categories,
        "feedback": feedback
    }
    result = generate_queries.invoke(prompt_arg)
    print("Generated new cluster: ", result)
    metadata = {"deckId": deck_id}
    return Document(page_content=result, metadata=metadata)

def index_generated_cluster(cluster_document):
    print("Start of indexing cluster function, cluster indexed is:", cluster_document.page_content)
    text_splitter = RecursiveCharacterTextSplitter.from_tiktoken_encoder(
        chunk_size=300,
        chunk_overlap=50)
    splits = text_splitter.split_documents([cluster_document])
    vectorstore = Chroma.from_documents(documents=splits, embedding=OpenAIEmbeddings(), persist_directory="./chroma_cluster_db")

    print("storing cluster in postgres")
    store_cluster_with_deck_id(cluster_document.page_content, cluster_document.metadata["deckId"])
    print("End of indexing cluster function")
    return vectorstore


def index(batch_message):
    created_cards = [] #list of documents representing card
    deleted_cards = [] #list of documents representing deleted cards
    deleted_cards_ids = [] #list of strings
    updated_cards = [] #list of documents representing new card to replace
    updated_cards_ids = [] #list of strings
    feedback = [] #list of feedback messages
    categories = batch_message.categories
    for card in batch_message.cardUpdate:
        if card.updateType == "create":
            created_cards.append(format_flashcard(card))
        elif card.updateType == "delete":
            deleted_cards_ids.append(str(card.cardId))
        elif card.updateType == "update":
            updated_cards.append(format_flashcard(card))
            updated_cards_ids.append(str(card.cardId))
        elif card.updateType == "feedback":
            feedback.append(format_feedback(card.feedback))
    card_vectorstore = embed_cards(created_cards,deleted_cards_ids,updated_cards,updated_cards_ids,feedback)
    cluster = generate_cluster(created_cards, deleted_cards, updated_cards, categories, batch_message.deckName, batch_message.deckId, feedback)
    cluster_vectorstore = index_generated_cluster(cluster)
    return {"card_vectorstore":card_vectorstore, "cluster_vectorstore":cluster_vectorstore, "cluster": cluster.page_content}


class Recommendation(BaseModel):
    generated_question: str
    generated_answer: str

def generate_recommendation(state):
    print("Generating recommendation...")
    template = """The user has a deck of flashcards called {deck_name} and the deck is under these categories: {categories}. 
    So far, the user's understanding of the material related to the deck's name and categories can be summarized with 
    this document: {cluster}. Using what you know about the user's understanding of the material, generate a flashcard, 
    so a question and an answer, that tests the user on something that the user has yet to include in their deck 
    (this you would infer based on the summary document provided). Your generated question can test something high level/abstract. 
    For example, if the category was biology and the name was "molecular biology and disease", 
    a higher level question would be "What is the number one reason why gene expression analysis done on samples of healthy and 
    cancer patients is not enough find us a cure for cancer." 
    The answer to the higher level question would be: Cancer is a a multifactorial disease 
    that does not produce a homogenous gene expression signature across all cancer patients". 
    Your generated question can also test for small, low-level details that user might have missed. 
    For example, if the category was computer science and the deck name is "Programming with React", 
    a low level question would be: "Under the hood, how are react state changes tracked for rerendering?". 
    The answer would be "React states are all controlled by the setState function provided by useState, 
    so the rerendering process starts everytime a setState function is called."

    The flashcard generated must be in JSON format with two fields:
    - generated_question: the question
    - generated_answer: the answer
    """
    output_parser = PydanticOutputParser(pydantic_object=Recommendation)
    prompt = ChatPromptTemplate.from_template(template)
    generate = (
    prompt
    | ChatOpenAI(temperature=0.7)
    | output_parser
    )
    prompt_arg = {"deck_name":state.deck_name, "categories": state.categories, "cluster": state.cluster}
    if len(state.hallucinations) > 0:
        template = """The user has a deck of flashcards called {deck_name} and the deck is under these categories: {categories}. 
        So far, the user's understanding of the material related to the deck's name and categories can be summarized with 
        this document: {cluster}. The user has already generated a question and answer pair that tests the user on something that the user has 
        yet to include in their deck, but you have determined that this question and answer pair is not grounded in the facts and
        the hallucinations found so far in this process include: ${hallucinations}.
        Generate a new question and answer pair (also known as a flashcard) that tests the user on something 
        that the user has yet to include in their deck, 
        but this time make sure that it is grounded in the facts.
        After generating the question and answer pair, wait, and do not return it yet. Think about the 
        question and answer pair you generated and reason about whether or not it is grounded in the facts and
        whether or not it tests something new that the user has not yet considered in their current deck. 
        
        The flashcard generaetd must be in JSON format with two fields:
        - generated_question: the question
        - generated_answer: the answer
        """
        prompt_arg["hallucinations"] = state.hallucinations
    prompt = ChatPromptTemplate.from_template(template)
    recommendation = generate.invoke(prompt_arg)
    print("Generated recommendation:", recommendation)
    state.generated_question = recommendation.generated_question
    state.generated_answer= recommendation.generated_answer
    print("done generating recommendation and setting graph state")
    return state

def parse_qa(text: str):
    lines = text.strip().split("\n")
    question = lines[0].replace("Q: ", "", 1).strip()
    answer = lines[1].replace("A: ", "", 1).strip()
    return question, answer

def retrieve_similar_cards(state):
    result = state.card_embedding_retriever.invoke(f"""Q: {state.generated_question}\nA: {state.generated_answer}""")
    print(result)
    if (len(result) == 0):
        state.retrieved_question = ""
        state.retrieved_answer = ""
        return state
    question, answer = parse_qa(result[0].page_content)
    state.retrieved_question = question
    state.retrieved_answer = answer
    return state

def retrieve_cluster_chunk(state):
    result = state.cluster_embedding_retriever.invoke(f"""Q: {state.generated_question}\nA: {state.generated_answer}""")
    print(result)
    if (len(result) == 0 or not result):
        state.cluster_chunk = ""
        return state
    state.cluster_chunk = result[0].page_content
    return state

def grade_hallucination(state):
    return state
def track_hallucination(state):
    state.hallucinations.append(f"Question: {state.generated_question}, Answer: {state.generated_answer}")
    state.num_retries += 1
    state.generated_question = ""
    state.generated_answer = ""
    return state

def similarity_retry(state):
    state.num_retries += 1
    state.generated_question = ""
    state.generated_answer = ""
    return state

#edges
def compare_to_card_vectors(state):
    template = """Respond with a yes or no (lowercase). Is this pair of question: {generated_question} and answer: {generated_answer} testing the same thing as this pair of question: {retrieved_question} and answer:{retrieved_answer}?"""
    prompt = ChatPromptTemplate.from_template(template)
    generate_answer = (
    prompt
    | ChatOpenAI(temperature=0.7)
    | StrOutputParser()
    )
    prompt_arg = {"generated_question": state.generated_question, "generated_answer": state.generated_answer, "retrieved_question": state.retrieved_question, "retrieved_answer": state.retrieved_answer}
    result = generate_answer.invoke(prompt_arg)
    if result == "no":
        return "retrieve_cluster_chunk"
    else:
        return "similarity_retry"
def compare_to_cluster_vectors(state):
    template = """Respond with a simple yes or no (lowercase). Is this pair of question: {generated_question} and answer: {generated_answer} something that the user already knows given that this summary: {cluster}, which summarizes what the user knows in regards to these categories: {categories} and this deck name: {deck_name}. """
    prompt = ChatPromptTemplate.from_template(template)
    generate_answer = (
    prompt
    | ChatOpenAI(temperature=0.7)
    | StrOutputParser()
    )
    prompt_arg = {
        "generated_question": state.generated_question,
        "generated_answer": state.generated_answer,
        "cluster": state.cluster,
        "categories": state.categories,
        "deck_name": state.deck_name,
    }
    result = generate_answer.invoke(prompt_arg)
    if result == "yes":
        return "similarity_retry"
    else:
        return "grade_hallucination"
def find_hallucinations(state):
    template = """Respond with a simple yes or no (lowercase). Is this pair of question: {generated_question} and answer: {generated_answer} grounded in the facts?"""
    prompt = ChatPromptTemplate.from_template(template)
    grader_chain = (
    prompt
    | ChatOpenAI(temperature=0.7)
    | StrOutputParser()
    )
    prompt_arg = {
        "generated_question": state.generated_question,
        "generated_answer": state.generated_answer,
    }
    result = grader_chain.invoke(prompt_arg)
    if result == "no":
        return "track_hallucination"
    else:
        return "END"
    
def check_num_retries(state):
    if state.num_retries == 1:
        return "END"
    else:
        return "retrieve_similar_cards"

class RagGraphState(BaseModel):
    cluster: str
    generated_question: str
    generated_answer: str
    retrieved_question: str
    retrieved_answer: str
    cluster_chunk: str
    deck_name: str
    categories: List[str]
    card_embedding_retriever: BaseRetriever
    cluster_embedding_retriever: BaseRetriever
    hallucinations: List[str] = []
    num_retries: int
    

def compile_graph(batch_message):
    vector_stores = index(batch_message) #returns dict with keys "card_vectorstore" and "cluster_vectorstore"
    print("before creating retrievers")
    card_embedding_retriever = vector_stores["card_vectorstore"].as_retriever(search_type="similarity", k=1)
    print("after creating card retriever")
    cluster_embedding_retriever = vector_stores["cluster_vectorstore"].as_retriever(search_type="similarity", k=1)
    print("after creating cluster retriever")
    print("before creating initial state")
    
    print("after creating initial state")
    workflow = StateGraph(RagGraphState)
    print("before adding nodes")
    workflow.add_node("generate_recommendation", generate_recommendation)
    workflow.add_node("retrieve_similar_cards", retrieve_similar_cards)
    workflow.add_node("retrieve_cluster_chunk", retrieve_cluster_chunk)
    workflow.add_node("grade_hallucination", grade_hallucination)
    workflow.add_node("track_hallucination", track_hallucination)
    workflow.add_node("similarity_retry", similarity_retry)
    print("after adding nodes")

    print("before adding edges")
    workflow.add_edge(START,"generate_recommendation")
    print("after adding start edge")

    workflow.add_conditional_edges(
        "generate_recommendation",
        check_num_retries,
        {
            "END": END,
            "retrieve_similar_cards": "retrieve_similar_cards",
        }
    )
    print("after adding generate recommendation check retries conditional edge")
    workflow.add_conditional_edges(
        "retrieve_similar_cards",
        compare_to_card_vectors,
        {
            "retrieve_cluster_chunk": "retrieve_cluster_chunk",
            "similarity_retry": "similarity_retry",
        },
    )
    print("after adding retrieve similar cards edges")
    workflow.add_conditional_edges(
        "retrieve_cluster_chunk",
        compare_to_cluster_vectors,
        {
            "grade_hallucination": "grade_hallucination",
            "similarity_retry": "similarity_retry",
        },
    )
    print("after adding retrieve cluster chunk edges")

    workflow.add_conditional_edges(
        "grade_hallucination",
        find_hallucinations,
        {
            "track_hallucination" : "track_hallucination",
            "END": END,
        }
    )
    workflow.add_edge("track_hallucination", "generate_recommendation")
    workflow.add_edge("similarity_retry", "generate_recommendation")
    print("about to compile graph")
    app = workflow.compile()
    print("compiled graph")

    print("before setting state to run")
    state_to_run = RagGraphState(
        cluster = get_cluster_with_deck_id(batch_message.deckId),                 
        generated_question = "",
        generated_answer = "",
        retrieved_question = "",
        retrieved_answer = "",
        cluster_chunk =  "",
        deck_name = batch_message.deckName,
        categories = batch_message.categories,
        card_embedding_retriever = card_embedding_retriever,
        cluster_embedding_retriever = cluster_embedding_retriever,
        hallucinations = [],
        num_retries=0
    )
    print("after setting state to run")
    result = list(app.stream(state_to_run))
    return result[-1]

