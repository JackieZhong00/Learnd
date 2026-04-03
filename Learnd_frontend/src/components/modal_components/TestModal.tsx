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
    const [userAnswerInput, setUserAnswerInput] = useState<string>("")
    
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
          //fetch only cards due for review (not deck specific)
          fetchReviewCards()
        }
        catch (error) {
          console.log("error: " + error)
        }
      }
      else {
        try {
          //fetch decks cards with pagination 
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
    const gradeAnswerInput = async (card : FlashcardDTO) => {
      const requestObj : FlashcardDTO = {...card, answer : userAnswerInput}
      console.log("request object: " + JSON.stringify(requestObj))
      try {
        await axios.post("http://localhost:8080/api/flashcard/grade", requestObj, {withCredentials: true})
        setCardIndex(cardIndex + 1)
        console.log("graded user input successfully")
      } catch (error) {
        console.log("error grading user input: " + error)
      }
    }
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
      {showEndScreen ? (
        <div>
          <p>End of cards! You can close the review session now.</p>
          <button onClick={() => useTestModal.setFalse()}>Exit Session</button>
        </div>
      ) : cards.length == 0 ? (
        <div>
          No cards exist in the deck. Create cards for a review session.
        </div>
      ) : (
        <div>
          {isShowingAnswer && cardIndex <= cards.length - 1 ? (
            <div>
              <p>{cards[cardIndex].answer}</p>
              {cards[cardIndex].requiresUserInput ? (
                <p>Your Answer: {userAnswerInput}</p>
              ) : (
                <></>
              )}
              {cards[cardIndex].requiresUserInput ? (
                <></>
              ) : (
                <div className="">
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
              )}
            </div>
          ) : (
            <div>
              <p>{cards[cardIndex].question}</p>
              {cards[cardIndex].requiresUserInput ? (
                <textarea
                  name="userAnswerInput"
                  id="userAnswerInput"
                  onChange={(e) => setUserAnswerInput(e.target.value)}
                  value={userAnswerInput}
                ></textarea>
              ) : (
                <>/</>
              )}
              {cards[cardIndex].requiresUserInput ? (
                <button
                  onClick={() => {
                    setIsShowingAnswer(!isShowingAnswer)
                    gradeAnswerInput(cards[cardIndex])
                  }}
                >
                  Reveal Answer
                </button>
              ) : (
                <button
                  onClick={() => {
                    setIsShowingAnswer(!isShowingAnswer)
                  }}
                >
                  Reveal Answer
                </button>
              )}
              <button
                onClick={() => {
                  if (cardIndex + 1 < cards.length - 1) {
                    setCardIndex((prev) => prev + 1)
                    if (cardIndex + 1 == cards.length - 1 && !isReview) {
                      setFetchMore((prev) => !prev)
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


// response:  {'query': 'Act as an expert in ProgrammingCategory to find as much relevant, factual,\n    
// trustworthy information as possible to answer\n    the question: require user input question', 
// 'follow_up_questions': None, 
// 'answer': None, 
// 'images': [], 
// 'results': [{'url': 'https://www.lagrange.edu/Bulletin/index.pdf', 
// 'title': 'Table of Contents', 'content': 'Grade Basis: AL Credit hours: 3.0 Lecture hours: 3.0 Prerequisites: • THEA 1184 - Acting I • THEA 2110 - Introduction to Design • THEA 2330 - Script Analysis • THEA 2351 - Acting II Restrictions: • Permission of course can be granted from instructor THEA 4470 - Special Topics A series of courses designed to provide students with advanced material/study in elements of Theatre Arts. Grade Basis: AL Credit hours: 3.0 Lecture hours: 3.0 Prerequisites: 472 • ENGL 1101 - Rhetoric and Composition I • ENGL 1102 - Rhetoric and Composition II Restrictions: • In rotation • Students in all majors, as well as undeclared majors, are encouraged to enroll WRPS 2500 - Introduction to Creative Writing This course introduces undergraduates to the fundamentals of imaginative writing. Grade Basis: AL Credit hours: 3.0 Lecture hours: 3.0 Prerequisites: • ENGL 1101 - Rhetoric and Composition I • ENGL 1102 - Rhetoric and Composition II Restrictions: • Offered in Spring Terms WRPS 2550 - Internship in English Writing and Publication Studies (1-6 Hours) An opportunity for students to gain added early applied experience and insight in approved off-campus settings.', 
// 'score': 0.012821214, 
// 'raw_content': None}, 
// {'url': 'https://beaconny.gov/wp-content/uploads/2025/08/Beacon-Highway-Garage-Rooftop-Solar-Array-Bid-Packet.pdf', 
// 'title': 'BEACON HIGHWAY GARAGE ROOFTOP SOLAR ARRAY', 
// 'content': 'New York State Labor Law, Article 8, Section 220.3a requires that certain information regarding the awarding of public work contracts, be', 
// 'score': 0.0020587533, 
// 'raw_content': None}, 
// {'url': 'https://frcog.org/wp-content/uploads/2023/03/IFB-2023-2070-Dfld-Police-Station-HVAC-Improvements.pdf', 
// 'title': 'IFB 2023-2070 Dfld Police Station HVAC draft 2-28', 
// 'content': 'Other qualified bidders are encouraged to partner with disadvantaged businesses. The awarded contractor shall endeavor to meet Workforce.', 
// 'score': 0.0017856744, 
// 'raw_content': None}, 
// {'url': 'https://www.co.cumberland.nc.us/docs/default-source/county-manager-documents/budget/adopted-budget/previous-adopted-budgets/fy2024---adopted-budget.pdf?sfvrsn=af7a18eb_2', 
// 'title': 'Adopted Budget - Cumberland County', 
// 'content': '269 REFERENCE…………….……………[...]RNEY I DEPUTY FINANCE DIRECTOR INTERNAL SERVICES DIRECTOR 81 79,756.43 106,992.84 134,229.26 CHILD SUPPORT LEGAL MANAGER ITS APPLICATIONS MANAGER ITS ENTERPRISE SOLUTIONS MANAGER ITS INFRASTRUCTURE MANAGER 82 83,468.41 111,972.55 140,476.68 ADVANCED PRACTICE PROVIDER II CHIEF INFORMATION SECURITY OFFICER CHILD SUPPORT ENFORCEMENT DIRECTOR COMMUNITY DEVELOPMENT DIRECTOR COUNTY ENGINEER DIRECTOR OF ELECTIONS (A) EMERGENCY SERVICES DIRECTOR LIBRARY DIRECTOR PUBLIC HEALTH NURSING DIRECTOR III SOLID WASTE DIRECTOR STAFF ATTORNEY II 83 87,368.42 117,204.46 147,040.50 ATTORNEY II DEPUTY INNOVATION & TECHNOLOGY DIRECTOR PLANNING DIRECTOR 84 91,460.95 122,694.19 153,927.43 ADVANCED PRACTICE PROVIDER III AMERICAN RESCUE PROGRAM MANAGER BUDGET & PERFORMANCE DIRECTOR CHIEF DIVERSITY OFFICER COMMUNICATIONS DIRECTOR HUMAN RESOURCES DIRECTOR PHARMACIST TAX ADMINISTRATOR (A) 87 104,854.27 122,694.19 176,469.54 CHIEF INNOVATION & TECHNOLOGY SERVICES DIRECTOR JUSTICE SERVICES DIRECTOR PHARMACY MANAGER 88 109,757.40 147,239.78 184,722.15 COUNTY SOCIAL SERVICES DIRECTOR LOCAL HEALTH DIRECTOR 91 $120,996.07 $162,315.94 $203,635.81 COUNTY ATTORNEY 64DE 42,075.28 54,572.29 67,069.30 DETENTION OFFICER 66DE/LE 45,760.00 59,500.67 73,241.34 DETENTION CENTER CORPORAL DEPUTY SHERIFF 68DE/LE 49,783.90 64,882.64 79,981.38 DETENTION CENTER SERGEANT DEPUTY SHERIFF CORPORAL 69LE 51,932.66 67,756.60 83,580.55 DEPUTY SHERIFF DETECTIVE 71LE 56524.5953 73898.31861 91272.04192 DEPUTY SHERIFF DETECTIVE SERGEANT DEPUTY SHERIFF SERGEANT 72 DE 58,976.68 77,177.98 95,379.28 DETENTION CENTER LIEUTENANT 75LE 67,015.07 87,929.33 108,843.59 DEPUTY SHERIFF DETECTIVE LIEUTENANT DEPUTY SHERIFF LIEUTENANT 328 GRADE MININUM MID-POINT MAXIMUM JOB TITLE 76DE 67,905.40 90,823.48 113,741.55 DEPUTY DETENTION CENTER ADMINISTRATOR 78LE 74,154.40 99,181.51 124,208.61 DEPUTY SHERIFF CAPTAIN 80DE/LE 80,978.46 108,308.69 135,638.92 DETENTION CENTER ADMINISTRATOR DEPUTY SHERIFF MAJOR 83LE 92,409.86 123,598.19 154,786.52 CHIEF DEPUTY SHERIFF APPOINTED POSITIONS SALARY SCHEDULE CLERK TO THE BOARD OF COUNTY COMMISSIONERS (A) COUNTY ATTORNEY (A) COUNTY MANAGER (A) TAX ADMINISTRATOR (A) PHYSICIAN SALARY SCHEDULE PHYSICIAN DIRECTOR II-A PHYSICIAN DIRECTOR II-B PHYSICIAN III-A PHYSICIAN III-B PHYSICIAN III-C EXECUTIVE SALARY SCHEDULE ASSISTANT COUNTY MANAGER - COMMUNITY SUPPORT SERVICES ASSISTANT COUNTY MANAGER - ENVIRONMENTAL & COMMUNITY SAFETY ASSISTANT COUNTY MANAGER - GENERAL GOVERNMENT & STEWARDSHIP ASSISTANT COUNTY MANAGER - STRATEGIC MANAGEMENT & GOVERNMENTAL AFFAIRS CHIEF OF STAFF DEPUTY COUNTY MANAGER FINANCE', 'score': 0.001391234, 'raw_content': None}, {'url': 'https://apps.bainbridgewa.gov/WebLink/DocView.aspx?id=100899&dbid=0&repo=Bainbridge', 'title': '071321 CCAGN BUSINESS MEETING', 'content': '071321 CCAGN BUSINESS MEETINGCITY COUNCIL REGULAR BUSINESS MEETING TUESDAY, JULY 13, 2021 REMOTE MEETING ON ZOOM PLEASE CLICK THE LINK BELOW TO JOIN THE', 
// 'score': 0.0008263576, 
// 'raw_content': None}], 
// 'response_time': 1.99, 'request_id': '133d3cbd-2a57-4599-a8bc-0da1e6bbcebd'}