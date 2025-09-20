//page used to display all decks created under one category 
import logo from '../assets/learnd_logo.png'
import { useParams, useNavigate} from 'react-router-dom'
import { useEffect, useState} from 'react'
import axios from 'axios'
import CreateDeckModal from './modal_components/CreateDeckModal'
import useCreateDeckModal from '../hooks/useCreateDeckModal'
import useIsLoggedOut from '../hooks/useIsLoggedOut'
import { set } from 'zod/v4'
import { useQuery } from '@tanstack/react-query'

type Deck = {
  id: number,
  name: string,
  category: string
}

const fetchDecks = async () : Promise<Deck[]> => {
  const response = await axios.get(
    `http://localhost:8080/api/deck/getAllDecksByUser`,
    { withCredentials: true }
  )
  console.log(response)
  return response.data
}

const DeckHome = () => {
  const param = useParams()
  const navigate = useNavigate()
  const isLoggedOutHook = useIsLoggedOut()
  const [menuOpen, setMenuOpen] = useState<boolean>(false)
  const deckModalHook = useCreateDeckModal()
 
  const {data} = useQuery({
    queryKey: ["getDecks"],
    queryFn: fetchDecks
  })
  const decks = data ? data : []

  const deckButtonHandler = (deckName: string, deckId : number) => {
    navigate(`/${param.username}/${deckName}/${deckId}`)
  }
  //fetch most recent decknames from the database
  //fetch first 10 categories whose parent is the root category to display on the right side 
  return (
    <div className="w-screen h-screen bg-[radial-gradient(circle,_#BCA8A8_0%,_#837675_83%,_#847674_100%)]">
      <div className="">
        {deckModalHook.isOpen && (
          <div className="fixed w-screen h-screen flex items-center justify-center z-50">
            <CreateDeckModal />
          </div>
        )}
      </div>
      <div className="block flex flex-row">
        <div className="flex w-full h-[100px]">
          <div className="w-[80px] h-[60px] overflow-hidden mt-[30px] ml-[30px]">
            <img
              src={logo}
              alt="learnd_logo"
              className="w-full h-full object-cover rounded-[55px]"
            />
          </div>
        </div>
        <div className="flex justify-center items-center mr-[3vw]">
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
                <button
                  className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                  onClick={() => navigate(`/${param.username}`)}
                >
                  Profile
                </button>
                <button className="px-4 py-2 hover:bg-gray-100 cursor-pointer" onClick={() => navigate(`/${param.username}/settings`)}>
                  Settings
                </button>
                <button 
                className="px-4 py-2 hover:bg-gray-100 cursor-pointer" 
                onClick={() => {
                  try {
                    axios.post(`http://localhost:8080/api/user/logout`, null, {withCredentials: true})
                    console.log("successful logout")
                    isLoggedOutHook.setTrue()
                    navigate('/')
                  }
                  catch(error){console.log("log out failed, error is: " + error)}
                }}>
                  Log out
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
      <hr className="border border-t-1 border-black-500 " />
      <div className="block">
        <div className="ml-[50px] mt-[10px] inline-block rounded-[50px] px-[12px] py-[8px] bg-[#B4B483]">
          {param.username}'s deck portal
        </div>
      </div>
      <div className="flex flex-row mt-[20px]">
        <div className="flex flex-col w-1/5 h-full border border-black-500 h-screen">
          <div className="flex flex-row mb-[15px]">
            <input
              className="rounded-[50px] px-[12px] py-[8px] mt-[10px] ml-[10px] border-none w-[175px]"
              placeholder="Search Decks"
            />
            <button
              className="ml-[15px] mt-[10px] px-[12px] py-[8px]"
              onClick={() => {
                deckModalHook.openModal()
              }}
            >
              Create Deck
            </button>
          </div>
          {decks.length > 0 ? (
            <div className="">
              <div className="">
                {decks.slice(0, -1).map((deck) => (
                  <div className="">
                    <button
                      className="py-[10px] px-[10px] my-[10px] deckLabel cursor-pointer w-full"
                      onClick={() => deckButtonHandler(deck.name, deck.id)}
                    >
                      {deck.name}
                    </button>
                    <hr className="border border-t-1 border-black-500" />
                  </div>
                ))}
              </div>
              <button
                className="deckLabel py-[10px] px-[10px] my-[10px] cursor-pointer w-full"
                onClick={() =>
                  deckButtonHandler(decks[decks.length - 1].name, decks[decks.length-1].id)
                }
              >
                {decks[decks.length - 1].name}
              </button>
            </div>
          ) : (
            <></>
          )}
        </div>
        <div className="w-4/5 h-full border border-black-500 bg-[linear-gradient(to_top,_#847E5E_10%,_#89825A_30%,_#B4B483_100%)] h-screen"></div>
      </div>
    </div>
  )
}
export default DeckHome