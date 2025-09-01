import { useNavigate, useParams } from 'react-router-dom'
import { useEffect, useState, useRef } from 'react'
import useCreateCardModal from '../hooks/useCreateCardModal'
import CreateCardModal from './modal_components/CreateFlashCardModal'
import RecommendModal from './modal_components/RecommendModal'
import logo from '../assets/learnd_logo.png'
import axios from 'axios'
import toast, { Toaster } from 'react-hot-toast'
import CreateFreeResponseCardModal from './modal_components/CreateFreeResponseCardModal'
import CreateMultipleChioceCardModal from './modal_components/CreateMultipleChioceCardModal'
import CreateFlashCardModal from './modal_components/CreateFlashCardModal'
import { set } from 'zod/v4'
import {CardSubmitType} from './modal_components/CreateFlashCardModal'
import useTestModalState from '../hooks/useTestModal'
import TestModal from './modal_components/TestModal'

export type FlashcardDTO = {
  id: number
  question: string
  answer: string
  dateOfNextUsage: Date
}

export type CardUpdateType = {
    question: string
    answer: string
}

export const emptyFlashcardDTO: FlashcardDTO = {
  question: '',
  answer: '',
  id: 0,
  dateOfNextUsage: new Date(),
}

type DeckRenameType = {
  name: string
}
const Deck = () => {
  const createCardModal = useCreateCardModal()
  const useTestModal = useTestModalState()
  const param = useParams()
  const navigate = useNavigate()
  const [cardSearch, setCardSearch] = useState<string>('')
  const [isEditingName, setIsEditingName] = useState<boolean>(false)
  const [deckName, setDeckName] = useState<string | undefined>(param.deck_name)
  const [tempDeckName, setTempDeckName] = useState<string | undefined>(deckName)
  const [menuOpen, setMenuOpen] = useState<boolean>(false)
  const [cards, setCards] = useState<FlashcardDTO[]>([])
  const [toDisplay, setToDisplay] = useState<FlashcardDTO>(emptyFlashcardDTO)
  const [questionToDisplay, setQuestionToDisplay] = useState<string>('')
  const [answerToDisplay, setAnswerToDisplay] = useState<string>('')
  const [dropDown, setDropDown] = useState<boolean>(false)
  const [cardTypeSelected, setCardTypeSelected] = useState<string>('')
  const [cardSearchResults, setCardSearchResults] = useState<FlashcardDTO[]>([])
  const [pageNumber, setPageNumber] = useState<number>(0)
  const [maxPageIndex, setMaxPageIndex] = useState<number>(0)
  const [isRecommendationDisplayed, setIsRecommendationDisplayed] = useState<boolean>(false)

    console.log("pageNumber:  " + pageNumber)
  useEffect(() => {
    if (cardSearch.trim() === '') {
      //trim removes all space characters
      setCardSearchResults([]) // clear suggestions if input is empty
      return // exit early
    }
    const delay = setTimeout(async () => {
      try {
        if (cardSearch.length > 0) {
          const response = await axios.get(
            `http://localhost:8080/api/flashcard/getWithPrefix/${encodeURIComponent(
              cardSearch
            )}`,
            { withCredentials: true }
          )
          if (Array.isArray(response.data)) {
            setCardSearchResults(response.data)
            console.log('cards with prefix:', response.data)
          } else {
            console.warn('Expected an array but got:', response.data)
            setCardSearchResults([])
          }
        }
      } catch (error) {
        console.error('Error fetching cards:', error)
      }
    }, 300)

    return () => clearTimeout(delay)
  }, [cardSearch])

  const outsideClickRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handleOutsideClick = (e: MouseEvent) => {
        if (
          outsideClickRef.current != null &&
          !outsideClickRef.current.contains(e.target as Node)
        ) {
          setIsEditingName(false)
          console.log('clicked outside')
        } else {
          setIsEditingName(true)
          console.log('clicked inside')
        }
    }
    document.addEventListener("click", handleOutsideClick, true)
    return () => document.removeEventListener("click", handleOutsideClick, true)
  }, [])

  const handleRename = async (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key == 'Enter') {
      try {
        const newNameObject: DeckRenameType = { name: tempDeckName as string }
        console.log('deck id: ' + param.deck_id)
        await axios.patch(
          `http://localhost:8080/api/deck/rename/${param.deck_id}`,
          newNameObject,
          { withCredentials: true }
        )
        setIsEditingName(false)
        setDeckName(tempDeckName)
        toast.success('Deck renamed!')
        navigate(`/${param.username}/${param.deck_name}/${param.deck_id}`)
      } catch (error) {
        console.log('failed to rename deck upon pressing enter ')
      }
    } else {
    }
  }

  //delete deck by id and navigate user back to deck portal
  const deleteDeck = async () => {
    try {
      await axios.delete(
        `http://localhost:8080/api/deck/delete/${param.deck_id}`,
        { withCredentials: true }
      )
      navigate(`/${param.username}/deck_home`)
      console.log('successfully deleted deck')
    } catch (error) {
      console.log('not able to delete deck, error was: ' + error)
    }
  }
  const deleteCard = async () => {
    try {
      await axios.delete(`http://localhost:8080/api/flashcard/deleteById/${toDisplay.id}`, { withCredentials: true })
      toast.success('Card deleted successfully!')
    } catch (error) {
      toast.error('failed to delete card')
      console.log('failed to delete card')
    }
  }
  const loadMoreCards = async () => {
    try {
        const response = await axios.get(
          `http://localhost:8080/api/flashcard/getDecksCards/${param.deck_id}?pageNumber=${pageNumber+1}&pageSize=10`,
          { withCredentials: true }
        )
        console.log("response of loadMoreCards: ", response)
        setPageNumber(pageNumber + 1)
        setCards(response.data.content)
    } catch(error) {
        console.log('failed to load more cards')
    }
  }

  const loadPreviousCards = async () => {
    try {
        if(pageNumber == 0) {
            toast.error('No previous cards to load')
            return
        }
        const response = await axios.get(
          `http://localhost:8080/api/flashcard/getDecksCards/${param.deck_id}?pageNumber=${pageNumber-1}&pageSize=10`,
          { withCredentials: true }
        )
        setPageNumber(pageNumber - 1)
        setCards(response.data.content)

        console.log('previous cards loaded: ', response)
    } catch(error) {
        console.log('failed to load previous cards')
    }
  }
  //use this upon mount to fetch 10 questions, using pagination
  //should first get deckid using deck name and user and then user deckId to fetch for cards
  useEffect(() => {
    const fetchPage = async () => {
      try {
        try {
          const pageResponse = await axios.get(
            `http://localhost:8080/api/flashcard/getDecksCards/${param.deck_id}?pageNumber=${pageNumber}&pageSize=10`
          , {withCredentials: true})
          setCards(pageResponse.data.content)
          setMaxPageIndex(pageResponse.data.page.totalPages - 1)
          console.log('cards fetched: ', pageResponse)
        } catch (error) {
          console.log('failed to fetch cards with deckid')
        }
      } catch (error) {
        console.log('failed to fetch deckid')
      }
    }
    fetchPage()
  }, [])

  const handleCardUpdate = async (e : React.FormEvent) => {
    e.preventDefault()
    if (toDisplay === emptyFlashcardDTO) {
      toast.error('No card selected to update')
      return
    }
    try {
        const cardUpdate : CardUpdateType = {question : questionToDisplay, answer: answerToDisplay}
        const response = await axios.patch(
          `http://localhost:8080/api/flashcard/updateCard/${toDisplay.id}`,
          cardUpdate,
          { withCredentials: true }
        )
        console.log('card updated: ', response)
        toast.success('Card updated successfully!')
    } catch(error){
        toast.error('failed to update card')
    }
  }
  const handleAddRecommendation = async (e : React.FormEvent) => {
    e.preventDefault()
    try {
      const card: CardSubmitType = { question: toDisplay.question, answer: toDisplay.answer}
      await axios.post(
        `http://localhost:8080/api/flashcard/${param.deck_id}/createcard`,
        card,
        { withCredentials: true }
      )
      toast.success('Flashcard Created!')
      setIsRecommendationDisplayed(false)
    } catch (error) {
      console.log('error: ' + error)
      toast.error("couldn't create the card")
    }
  }

  
  const childSetIsRecommendationDisplayed = (choice : boolean) => {
    setIsRecommendationDisplayed(choice)
  }

  
  return (
    <div className="w-screen h-screen bg-[radial-gradient(circle,_#BCA8A8_0%,_#837675_83%,_#847674_100%)]">
      <Toaster position="top-center" />
      {useTestModal.isOpen ? (
        <TestModal />
      ) : (<div></div>)}
      <RecommendModal
        deckId={param.deck_id}
        setQuestionToDisplay={setQuestionToDisplay}
        setAnswerToDisplay={setAnswerToDisplay}
        childSetIsRecommendationDisplayed={childSetIsRecommendationDisplayed}
        setToDisplay={setToDisplay}
      />
      {createCardModal.isOpen && cardTypeSelected === 'flashcard' && (
        <CreateFlashCardModal />
      )}
      {createCardModal.isOpen && cardTypeSelected === 'multipleChoice' && (
        <CreateMultipleChioceCardModal />
      )}
      {createCardModal.isOpen && cardTypeSelected === 'freeResponse' && (
        <CreateFreeResponseCardModal />
      )}

      <div className="block flex flex-row h-[10vh]">
        <div className="flex w-full h-[100px]">
          <div className="w-[80px] h-[60px] overflow-hidden mt-[30px] ml-[30px]">
            <img
              src={logo}
              alt="learnd_logo"
              className="w-full h-full object-cover rounded-[55px]"
            />
          </div>
        </div>
        <div className="flex justify-center items-center mr-[3vw] h-[10vh]">
          {!menuOpen && (
            <button
              onClick={() => setMenuOpen(!menuOpen)}
              className="w-[2vw] h-[3.5vh]"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.5}
                stroke="currentColor"
                className="size-6"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"
                />
              </svg>
            </button>
          )}
          {menuOpen && (
            <div className="absolute right-[3vw] top-[3vh] mt-2 w-48 bg-white rounded-md shadow-lg flex flex-col">
              <div className="flex ml-auto">
                <button
                  onClick={() => {
                    setMenuOpen(!menuOpen)
                  }}
                  className="bg-[#D5627F] mb-[10px]"
                >
                  x
                </button>
              </div>
              <div className="flex flex-row gap-[10px]">
                <button className="px-4 py-2 hover:bg-gray-100 cursor-pointer">
                  Profile
                </button>
                <button
                  className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                  onClick={() => {
                    try {
                      axios.post(
                        'http://localhost:8080/api/user/logout',
                        null,
                        { withCredentials: true }
                      )
                    } catch (error) {
                      console.log('error with logging out: ' + error)
                    }
                  }}
                >
                  Log out
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
      <hr className="border border-t-1 border-black-500 " />
      <div className="flex flex-row gap-[2vw] mx-[2vw] h-[8vh]">
        <p className="flex rounded-[50px] w-[8vw] h-[4vh] bg-[#B4B483] text-center justify-center items-center">
          {param.username}'s deck:
        </p>
        <div className="flex flex-col mt-[1.5vh]" ref={outsideClickRef}>
          {isEditingName ? (
            <input
              className="rounded-[50px] bg-[#B4B483] flex border border-none w-[10vw] h-[4vh] text-[16px]"
              onChange={(e) => {
                setTempDeckName(e.target.value)
              }}
              onKeyDown={handleRename}
              autoFocus
              type="text"
              value={tempDeckName}
            />
          ) : (
            <input
              readOnly
              className="cursor-pointer bg-[#B4B483] rounded-[50px] border-none deckName text-center w-[10vw] h-[4vh] text-[16px]"
              value={deckName}
            />
          )}
          <button
            className="cursor-pointer flex justify-content items-center rounded-[50px] w-[4vw] h-[2vh] text-center text-[9px] mt-[5px]"
            onClick={deleteDeck}
          >
            Delete Deck
          </button>
        </div>
        <button className='h-[5vh] mt-[1vh]' onClick={() => {useTestModal.setTrue()}}>Start Review</button>
      </div>
      <div className="max-h-[70vh]">
        <div className="flex flex-row mt-[20px] max-h-[70vh]">
          <div className="flex flex-col w-1/5 border border-black-500 h-[60vh]">
            <div className="flex flex-row mb-[15px]">
              <input
                className="rounded-[50px] px-[12px] py-[8px] mt-[10px] ml-[10px] border-none w-[175px]"
                onChange={(e) => {
                  setCardSearch(e.target.value)
                }}
                placeholder="Search Cards"
              />
              <button
                className="ml-[15px] mt-[10px] px-[12px] py-[8px] cursor-pointer relative"
                onClick={() => {
                  setDropDown(!dropDown)
                }}
              >
                Create Card
                {dropDown && (
                  <div className="absolute left-[0%] bg-[#8BB4CD] py-[10px]">
                    <div className="max-h-[150px] overflow-y-auto flex flex-col">
                      <div
                        className="text-[#000000] bg-[#8BB4CD]"
                        onClick={() => {
                          setCardTypeSelected('flashcard')
                          createCardModal.openModal()
                        }}
                      >
                        Flashcard
                      </div>
                      <hr className="border border-t-1 border-black-500 w-full" />
                      <div
                        className="text-[#000000] bg-[#8BB4CD]"
                        onClick={() => {
                          setCardTypeSelected('flashcard')
                          createCardModal.openModal()
                        }}
                      >
                        Multiple Choice
                      </div>
                      <hr className="border border-t-1 border-black-500 w-full" />
                      <div
                        className="text-[#000000] bg-[#8BB4CD]"
                        onClick={() => {
                          setCardTypeSelected('flashcard')
                          createCardModal.openModal()
                        }}
                      >
                        Free Response
                      </div>
                    </div>
                  </div>
                )}
              </button>
            </div>
            {cards.length > 0 ? (
              <div className="overflow-y-auto max-h-[70vh]">
                {pageNumber > 0 ? (
                  <button
                    onClick={loadPreviousCards}
                    className="cursor-pointer"
                  >
                    Go Back
                  </button>
                ) : (
                  <div></div>
                )}
                <div className="">
                  {cards.slice(0, -1).map((card) => (
                    <div className="">
                      <button
                        className="py-[10px] px-[10px] my-[10px] deckLabel cursor-pointer w-full"
                        onClick={() => {
                          setToDisplay(card)
                          setQuestionToDisplay(card.question)
                          setAnswerToDisplay(card.answer)
                          setIsRecommendationDisplayed(false)
                        }}
                      >
                        {card.question.slice(0, 25)}
                      </button>
                      <hr className="border border-t-1 border-black-500" />
                    </div>
                  ))}
                </div>
                <button
                  className="deckLabel py-[10px] px-[10px] my-[10px] cursor-pointer w-full"
                  onClick={() => {
                    setToDisplay(cards[cards.length - 1])
                    setQuestionToDisplay(cards[cards.length - 1].question)
                    setAnswerToDisplay(cards[cards.length - 1].answer)
                    setIsRecommendationDisplayed(false)
                  }}
                >
                  {cards[cards.length - 1].question.slice(0, 25)}
                </button>
              </div>
            ) : (
              <div></div>
            )}
            {pageNumber != maxPageIndex ? (
              <button onClick={loadMoreCards} className="cursor-pointer">
                Load More
              </button>
            ) : (
              <div></div>
            )}
          </div>
          <div className="w-4/5 h-[77vh] border border-black-500 bg-[#D9D9D9]">
            <button
              className="flex w-[2vw] h-[3vh] cursor-pointer"
              onClick={deleteCard}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.5}
                stroke="currentColor"
                className="size-6"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0"
                />
              </svg>
            </button>
            {isRecommendationDisplayed ? (
              <form onSubmit={handleCardUpdate}>
                <div className="flex flex-col">
                  <p className="text-[#9A01FF] ml-[2vw]">Question</p>
                  <div className="flex justify-center items-center">
                    <textarea
                      onChange={(e) => setQuestionToDisplay(e.target.value)}
                      className="block w-15/16 h-[20vh] border border-color-[#000000] mx-[1vw] my-[1vh] rounded-[8px] bg-[#A49F9E]"
                      value={questionToDisplay}
                    />
                  </div>
                </div>
                <div className="flex flex-col">
                  <p className="text-[#22C372] ml-[2vw]">Answer</p>
                  <div className="flex items-center justify-center">
                    <textarea
                      onChange={(e) => setAnswerToDisplay(e.target.value)}
                      className="w-15/16 h-[30vh] border border-color-[#000000] mx-[1vw] my-[1vh] rounded-[8px] bg-[#A49F9E]"
                      value={answerToDisplay}
                    />
                  </div>
                </div>
                <button type="submit" className="cursor-pointer">
                  Update
                </button>
              </form>
            ) : (
              <form onSubmit={handleAddRecommendation}>
                <div className="flex flex-col">
                  <p className="text-[#9A01FF] ml-[2vw]">Question</p>
                  <div className="flex justify-center items-center">
                    <textarea
                      onChange={(e) => setQuestionToDisplay(e.target.value)}
                      className="block w-15/16 h-[20vh] border border-color-[#000000] mx-[1vw] my-[1vh] rounded-[8px] bg-[#A49F9E]"
                      value={questionToDisplay}
                    />
                  </div>
                </div>
                <div className="flex flex-col">
                  <p className="text-[#22C372] ml-[2vw]">Answer</p>
                  <div className="flex items-center justify-center">
                    <textarea
                      onChange={(e) => setAnswerToDisplay(e.target.value)}
                      className="w-15/16 h-[30vh] border border-color-[#000000] mx-[1vw] my-[1vh] rounded-[8px] bg-[#A49F9E]"
                      value={answerToDisplay}
                    />
                  </div>
                </div>
                <button type="submit" className="cursor-pointer">
                  Add Card
                </button>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Deck
