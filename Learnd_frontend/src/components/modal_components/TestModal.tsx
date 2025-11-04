import { useState, useEffect} from "react"
import axios from "axios"
import { FlashcardDTO } from "../Deck"
import { useParams } from "react-router-dom"
import { set } from "zod/v4"
import useTestModalState from "../../hooks/useTestModal"
import { get } from "react-hook-form"
import { ca } from "zod/v4/locales"

type TestModalProps = {
  isReview : boolean
}
const TestModal = ({isReview} : TestModalProps) => {
    const useTestModal = useTestModalState()
    const [cards, setCards] = useState<FlashcardDTO[]>([])
    const [showEndScreen, setShowEndScreen] = useState<boolean>(false)
    const [pageNumber, setPageNumber] = useState<number>(0)
    const [isShowingAnswer, setIsShowingAnswer] = useState<boolean>(false)
    const [cardIndex, setCardIndex] = useState<number>(0)
    const [sliderClicked, setSliderClicked] = useState<boolean>(false)
    const [sliderValue, setSliderValue] = useState<number>(0)
    const [fetchMore, setFetchMore] = useState<boolean>(false)
    
    const param = useParams()
    
    const fetchCards = async () => {
      console.log("fetching more...")
      const response = await axios.get(
        `http://localhost:8080/api/flashcard/getDecksCards/${
          param.deck_id
        }?pageNumber=${pageNumber}&pageSize=100`,
        { withCredentials: true }
      )
      if (response.data.content.length == 0) {
        setShowEndScreen(true)
        return
      }
      setPageNumber(pageNumber + 1)
      setCards(response.data.content)
    }

    //isReview is set to true and this modal is rendered in DeckHome.tsx component - so user can review all cards that are due 
    //is Review is set to false in deck.tsx, which mean only deck specific cards are fetched 
    useEffect(() => {
      if (isReview) {
        try {
          fetchReviewCards()
        }
        catch (error) {
          console.log("error: " + error)
        }
      }
      else {
        try {
            fetchCards()
        } catch (error) {
            console.log("error: " + error)
            // setPageNumber(pageNumber - 1)
            // setTimeout(fetchCards, 1000)
        }
      }
    }, [fetchMore])

    const fetchReviewCards = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/flashcard/getAllDueCards`, {withCredentials: true})
        setCards(response.data)
      } catch (error) {
        console.log("error fetching review cards: " + error)
      }
    }
   
    const handleSubmitDifficulty = async () => {
        try {
            const numberOfDaysToAdd = Math.ceil(7*(sliderValue/10)) //7 days is the max 
            const currentDate = new Date()
            currentDate.setDate(currentDate.getDate() + numberOfDaysToAdd)
            const localDateString = currentDate.toISOString().split('T')[0]

            await axios.patch(
              `http://localhost:8080/api/flashcard/updateCardDate/${cards[cardIndex].id}`,
              localDateString,
              {
                withCredentials: true,
                headers: { 'Content-Type': 'application/json' },
              }
            )
            setIsShowingAnswer(false)
            setCardIndex(cardIndex + 1)
        } catch (error) {
            console.log("error: " + error)
        }
    }
    console.log(cards)
  return (
    <div className="absolute top-[10vh] w-[100vw] h-[90vh] z-[50] test-modal">
      <button
        className="cursor-pointer"
        onClick={() => {
          useTestModal.setFalse()
        }}
      >
        Exit
      </button>
      {showEndScreen ? 
      ( <div>
          <p>End of cards! You can close the review session now.</p>
          <button onClick={() => useTestModal.setFalse()}>Exit Session</button>
        </div>
      ) :
      cards.length == 0 ? (
        <div>
          No cards exist in the deck. Create cards for a review session.
        </div>
      ) : (
        <div>
          {isShowingAnswer && cardIndex != cards.length - 1 ? (
            <div>
              <p>{cards[cardIndex].answer}</p>
              <input
                type="range"
                min="0"
                max="10"
                name="difficulty-slider"
                className="background-[#000000] accent-[#000000]"
                onChange={(e) => {
                  setSliderClicked(true)
                  setSliderValue(parseInt(e.target.value, 10))
                }}
              />
              <div className="">0</div>
              <div className="">10</div>
              {sliderClicked ? (
                <button
                  onClick={handleSubmitDifficulty}
                  className="cursor-pointer"
                >
                  Next
                </button>
              ) : (
                <p>Rank the card's difficulty to move onto the next card</p>
              )}
            </div>
          ) : (
            <div>
              <p>{cards[cardIndex].question}</p>
              <button
                onClick={() => {
                  setIsShowingAnswer(!isShowingAnswer)
                }}
              >
                Reveal Answer
              </button>
              <button
                onClick={() => {
                  console.log("card index: " + cardIndex)
                  console.log("cards length: " + cards.length)
                  if (cardIndex + 1 <= cards.length-1) {
                    setCardIndex(prev=>prev + 1)
                    console.log("card index after plus 1: " + cardIndex)
                    if (cardIndex + 1 == cards.length - 1 && !isReview) {
                      console.log("setting fetchmore, from: " + fetchMore)
                      setFetchMore((prev) => !prev)
                      console.log("fetchmore set to: " + fetchMore)
                    }
                  }
                }}
              >
                Skip
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
export default TestModal