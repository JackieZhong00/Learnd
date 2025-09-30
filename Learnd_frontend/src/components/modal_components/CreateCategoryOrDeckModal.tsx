import { useState, useEffect } from "react"
import toast, { Toaster } from 'react-hot-toast'
import axios from "axios"
import useCreateDeckModal from "../../hooks/useCreateDeckModal"
import { set } from "zod/v4"
type deck = {
  id: number
  name: string
  category: string
}
type category = {
  name: string
}
const CreateCategoryOrDeckModal = () => {
  const [name, setDeckName] = useState<string>("")
  const [categoryName, setCategory] = useState<string>("")
  const [deckNamesWithPrefix, setDeckNamesWithPrefix] = useState<deck[]>([])
  const [categoryNamesWithPrefix, setCategoryNamesWithPrefix] = useState<category[]>([])
  const [categoryError, setCategoryError] = useState<boolean>(false)
  
  const deckModalHook = useCreateDeckModal()

  const handleNameChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setDeckName(event.target.value)
  }

  const handleCategoryChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setCategory(event.target.value)
  }
  const callCreateCategoryEndpoint = async () => {
    try {
      await axios.post(
        `http://localhost:8080/api/category/create`,
        { name: categoryName },
        { withCredentials: true }
      )
    } catch (error) {
      console.log('error with creating category')
    }
  }
  const callCreateDeckEndpoint = async () => {
    try {
      await axios.post(
        `http://localhost:8080/api/deck/createDeck`,
        { name: name, categoryName: categoryName },
        { withCredentials: true }
      )
      toast.success('Deck and category created successfully')
      setDeckName('')
      setCategory('')
    } catch (error) {
      console.log('deck could not be created')
    }
  }

  const handleSubmit = async (e : React.FormEvent) => {
    e.preventDefault()
    if(categoryName === "" && name === "") {
      toast.error("Please fill in all fields")
      return
    }
    if (categoryName !== "" && name === "") {
      await callCreateCategoryEndpoint()
      toast.success("Category created successfully")
      setCategory("")
      return
    }
    try {
      await callCreateCategoryEndpoint()
      await callCreateDeckEndpoint()
    } catch (error) {
      console.log("couldn't create deck or category")
    }

  }


  useEffect(() => {
    if (name.trim() === '') { //trim removes all space characters 
      setDeckNamesWithPrefix([]) // clear suggestions if input is empty
      return // exit early
    }
    const delay = setTimeout(async () => {
      try {
        if (name.length > 0) {
          const response = await axios.get(
            `http://localhost:8080/api/deck/getDeckNamesWithPrefix/${encodeURIComponent(
              name
            )}`,
            { withCredentials: true }
          )
          if (Array.isArray(response.data)) {
            setDeckNamesWithPrefix(response.data)
            console.log("Deck names with prefix:", response.data)
          } else {
            console.warn('Expected an array but got:', response.data)
            setDeckNamesWithPrefix([])
          }
        }
      } catch (error) {
        console.error("Error fetching deck names:", error)
      }
    }, 300)

    return () => clearTimeout(delay)
  }, [name])

  useEffect(() => {
    if (categoryName.trim() === '') {
      setCategoryNamesWithPrefix([]) // clear suggestions if input is empty
      return // exit early
    }
    const delay = setTimeout(async () => {
      try {
        if (categoryName.length > 0) {
          const response = await axios.get(
            `http://localhost:8080/api/category/searchPrefix/${encodeURIComponent(categoryName)}`,
            { withCredentials: true }
          )
          if (Array.isArray(response.data)) {
            setCategoryNamesWithPrefix(response.data)
            console.log('Category names with prefix:', response.data)
          } else {
            console.warn('Expected an array but got:', response.data)
            setCategoryNamesWithPrefix([])
          }
        }
      } catch (error) {
        console.error('Error fetching category names:', error)
      }
    }, 300)
    return () => clearTimeout(delay)
  }, [categoryName])
  console.log("category names with prefix length: " + categoryNamesWithPrefix.length)
  
  return (
    <div className="bg-[#D9D9D9] w-[33vw] h-[27vh] border border-gray-300 border-2 rounded-[35px]">
      <Toaster position="top-center" />
      <form
        onSubmit={handleSubmit}
        className="flex flex-col gap-[5px] ml-[10px] mr-[10px]"
      >
        <div className="flex justify-between items-center">
          <p className="text-[20px] underline">Create Deck: </p>
          <button
            className="underline mr-[10px]"
            onClick={() => {
              deckModalHook.closeModal()
            }}
          >
            close
          </button>
        </div>
        <div className="flex flex-col gap-[25px]">
          <div className="flex flex-col gap-[5px] relative">
            <label htmlFor="categoryName">Category Name</label>
            <input
              id="categoryName"
              value={categoryName}
              className="rounded-[25px]"
              onChange={handleCategoryChange}
            ></input>
            {/* {categoryError && (<p>category already exists</p>)} */}
            {categoryNamesWithPrefix.length > 0 && (
              <div className="absolute top-[100%] left-0 bg-white border border-gray-300 rounded-md shadow-lg z-10">
                <div className="max-h-[150px] overflow-y-auto flex flex-col">
                  {categoryNamesWithPrefix.map((category, index) => (
                    <button
                      key={index}
                      className="px-4 py-2 hover:bg-[#D9D9D9] bg-[#FFFFFF] cursor-pointer rounded-none border border-bottom-[000000]"
                      onClick={() => {
                        setCategory(category.name)
                        setCategoryNamesWithPrefix([])
                      }}
                    >
                      {category.name}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
          <div className="flex flex-col gap-[5px]">
            <label htmlFor="deckName">Deck Name: </label>
            <div className="relative flex flex-col">
              <input
                id="deckName"
                value={name}
                className="rounded-[25px]"
                onChange={handleNameChange}
              ></input>
              {deckNamesWithPrefix.length > 0 && (
                <div className="absolute top-[100%] left-0 bg-white border border-gray-300 rounded-md shadow-lg z-10">
                  <div className="max-h-[150px] overflow-y-auto flex flex-col">
                    {deckNamesWithPrefix.map((deck, index) => (
                      <button
                        key={index}
                        className="px-4 py-2 hover:bg-[#D9D9D9] bg-[#FFFFFF] cursor-pointer rounded-none border border-bottom-[000000]]"
                        onClick={() => {
                          setDeckName(deck.name)
                          setDeckNamesWithPrefix([])
                        }}
                      >
                        {deck.name}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
          <button type="submit" className="flex w-1/5">
            submit
          </button>
        </div>
      </form>
    </div>
  )
}
export default CreateCategoryOrDeckModal