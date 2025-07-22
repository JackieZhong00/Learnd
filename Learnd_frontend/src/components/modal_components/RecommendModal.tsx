import axios from "axios"
import { useEffect, useState } from "react"
import { FlashcardDTO, emptyFlashcardDTO } from "../Deck"

type Props = {
  deckId: string | undefined
  allowRecommendationToDisplay: (card: FlashcardDTO) => void
  childSetIsRecommendationDisplayed: (chioce : boolean) => void
}




const RecommendModal = ({ deckId, allowRecommendationToDisplay, childSetIsRecommendationDisplayed }: Props) => {
  const [recommendation, setRecommendation] =
    useState<FlashcardDTO>(emptyFlashcardDTO)
  const [isDisplayingQuestion, setIsDisplayingQuestion] =
    useState<boolean>(false)

  useEffect(() => {
    let isCancelled = false;

    const fetchAndSchedule = async () => {
        try {
        const response = await axios.get(
            `http://localhost:8080/api/recommend/getRecommendation/${deckId}`,
            { withCredentials: true }
        );
        if (!isCancelled) {
            setRecommendation(response.data);
            console.log('Fetched recommendation');
        }
        } catch (error) {
        console.error('Error fetching recommendation:', error);
        }

        if (!isCancelled) { //always false until we set to true 
            setTimeout(fetchAndSchedule, 30000); //30secs
        }
    };

    fetchAndSchedule(); //start the recursive fetching

    return () => {
        isCancelled = true;
    };
  }, []);

  return (
    <div>
      <button onClick={() => setIsDisplayingQuestion(!isDisplayingQuestion)}>
        {isDisplayingQuestion ? (
          <div>Question preview: {recommendation.question}</div>
        ) : (
          <div>Answer preview: {recommendation.answer}</div>
        )}
      </button>
      <button onClick={() => {
        allowRecommendationToDisplay(recommendation)
        childSetIsRecommendationDisplayed(true)
      }}>Display card</button>
    </div>
  )
}
export default RecommendModal