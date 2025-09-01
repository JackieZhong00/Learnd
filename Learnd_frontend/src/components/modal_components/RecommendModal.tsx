import axios from "axios"
import { useEffect, useState } from "react"
import { FlashcardDTO, emptyFlashcardDTO } from "../Deck"
import { useParams } from "react-router-dom"
import toast, { Toaster } from 'react-hot-toast'
import { set } from "zod/v4"
import { CardSubmitType } from './CreateFlashCardModal'


type Props = {
  deckId: string | undefined
  childSetIsRecommendationDisplayed: (chioce: boolean) => void
  setQuestionToDisplay: (question: string) => void
  setAnswerToDisplay: (answer: string) => void
  setToDisplay : (card : FlashcardDTO) => void
}

type RecommendFeedbackEvent = {
  wasAccepted : boolean
  userId : number
  deckId : number
  question : string
  answer : string
  isMultipleChoice : boolean
}



const RecommendModal = ({
  deckId,
  childSetIsRecommendationDisplayed,
  setQuestionToDisplay,
  setAnswerToDisplay,
  setToDisplay
}: Props) => {
  const [recommendation, setRecommendation] =
    useState<FlashcardDTO>(emptyFlashcardDTO)
  const [isDisplayingQuestion, setIsDisplayingQuestion] =
    useState<boolean>(true)
  const param = useParams()
  let user_id: number = 0

  useEffect(() => {
    const getUserId = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/user/getUserId/${param.username}`,
          { withCredentials: true }
        )
        user_id = response.data
      } catch (error) {
        console.log('Error fetching user ID:', error)
      }
    }
    getUserId()
  }, [])

  // useEffect(() => {
  //   let isCancelled = false;

  //   const fetchAndSchedule = async () => {
  //       try {
  //       const userId = (await axios.get(`http://localhost:8080/api/user/getUserId/${param.username}`, { withCredentials: true })).data;
  //       const response = await axios.get(
  //         `http://localhost:8080/api/recommend/getRecommendations/${userId}/${deckId}/${param.deck_name}`,
  //         { withCredentials: true }
  //       )
  //       console.log('Fetched recommendation:', response.data);
  //       if (!isCancelled) {
  //           setRecommendation(response.data);
  //           console.log('Fetched recommendation');
  //       }
  //       } catch (error) {
  //       console.error('Error fetching recommendation:', error);
  //       }

  //       if (!isCancelled) { //always false until we set to true
  //           setTimeout(fetchAndSchedule, 30000); //30secs
  //       }
  //   };

  //   fetchAndSchedule(); //start the recursive fetching

  //   return () => {
  //       isCancelled = true;
  //   };
  // }, []);

  const handleClick = async () => {
    try {
      const userId = (
        await axios.get(
          `http://localhost:8080/api/user/getUserId/${param.username}`,
          { withCredentials: true }
        )
      ).data
      const recommendation = await axios.get(
        `http://localhost:8080/api/recommend/getRecommendations/${userId}/${deckId}/${param.deck_name}`,
        { withCredentials: true }
      )
      console.log('Fetched recommendation:', recommendation.data)
      setRecommendation(recommendation.data)
    } catch (error) {
      console.error('Error fetching recommendation:', error)
    }
  }
  const pollKafkaCardEvents = async () => {
    try {
      const userId = (
        await axios.get(
          `http://localhost:8080/api/user/getUserId/${param.username}`,
          { withCredentials: true }
        )
      ).data
      const response = await axios.get(
        `http://localhost:8080/api/recommend/pollKafkaEvents/${userId}/${deckId}/${param.deck_name}`,
        { withCredentials: true }
      )
      console.log('Polled Kafka card events:', response.data)
    } catch (error) {
      console.error('Error polling Kafka card events:', error)
    }
  }
  const handleAdd = async () => {
    try {
      const card: CardSubmitType = { question: recommendation.question, answer: recommendation.answer }
      await axios.post(
        `http://localhost:8080/api/flashcard/${param.deck_id}/createcard`,
        card,
        { withCredentials: true }
      )
      toast.success('Flashcard Created!')
      childSetIsRecommendationDisplayed(false)
    } catch (error) {
      console.log('error: ' + error)
      toast.error("couldn't create the card")
    }
  }
  const sendRejectRecommendFeedback = async () => {
    try {
      const recommendationFeedbackEvent: RecommendFeedbackEvent = {
        wasAccepted : false,
        userId : user_id,
        deckId : Number(param.deck_id),
        question : recommendation.question,
        answer : recommendation.answer,
        isMultipleChoice : false
      }
      await axios.post(
        `http://localhost:8080/sendRecommendFeedback`,
        recommendationFeedbackEvent,
        { withCredentials: true }
      )
      toast.success('Feedback sent!')
    } catch (error) {
      console.log('Error sending recommendation feedback:', error)
    }
  }
  return (
    <div className="absolute right-[0vw] top-[11vh] border border-[#000000]">
      <Toaster position="top-center" />
      <div
        onClick={() => setIsDisplayingQuestion(!isDisplayingQuestion)}
        className="cursor-pointer border border-[#000000] w-[54vw] h-[8vh]"
      >
        {isDisplayingQuestion ? (
          <div className="text-[15px]">
            Question preview: {recommendation.question}
          </div>
        ) : (
          <div className="text-[15px]">
            Answer preview: {recommendation.answer}
          </div>
        )}
      </div>
      <button
        onClick={() => {
          childSetIsRecommendationDisplayed(false)
          setQuestionToDisplay(recommendation.question)
          setAnswerToDisplay(recommendation.answer)
          setToDisplay(recommendation)
        }}
        className="cursor-pointer"
      >
        Edit
      </button>
      <button onClick={handleAdd} className="cursor-pointer">Add Card</button>
      <button onClick={sendRejectRecommendFeedback} className="cursor-pointer">Reject Card</button>
      <button onClick={handleClick} className="cursor-pointer">
        Get a Recommendation
      </button>
      <button onClick={pollKafkaCardEvents} className="cursor-pointer">
        Poll Kafka Card Events
      </button>
    </div>
  )
}
export default RecommendModal