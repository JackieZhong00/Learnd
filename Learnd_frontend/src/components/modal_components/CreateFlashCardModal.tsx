import toast, {Toaster} from 'react-hot-toast'
import { useParams } from 'react-router-dom'
import { useState } from 'react'
import axios from 'axios'
import useCreateCardModal from '../../hooks/useCreateCardModal'


//import and use react hook form to register and submit the form - only validation required is that all fields must have something
//implement dropdown menu and autofill for category name and subject name

export type CardSubmitType = {
  question: string
  answer: string
}

const CreateFlashCardModal= () => {
  const param = useParams()
  const createCardModalHook = useCreateCardModal()
  const [question, setQuestion] = useState<string>("")
  const [answer, setAnswer] = useState<string>("")
  const handleSubmit = async (e : React.FormEvent ) => {
    e.preventDefault()
    try {
      const card : CardSubmitType = {question: question, answer: answer}
      await axios.post(`http://localhost:8080/api/flashcard/${param.deck_id}/createcard`, card, {withCredentials: true})
      toast.success("Flashcard Created!")
    } catch (error) {
      console.log("error: " + error)
      toast.error("couldn't create the card")
    }
  }
  return (
    <div className="bg-[#D9D9D9] w-[50vw] h-[60vh] absolute top-[23vh] left-[23vw] border border-gray-300 border-2 rounded-[35px]">
      <Toaster position="top-center" />
      <form
        onSubmit={handleSubmit}
        className="flex flex-col gap-[5px] ml-[10px] mr-[10px]"
      >
        <div className="flex justify-between items-center">
          <p className="text-[20px] underline">Flashcard</p>
          <button
            className="underline mr-[10px] cursor-pointer"
            onClick={() => {
              createCardModalHook.closeModal()
            }}
          >
            close
          </button>
        </div>
        <div className="flex flex-col gap-[25px]">
          <div className="flex flex-col gap-[5px]">
            <label htmlFor="question">Question</label>
            <textarea
              id="question"
              className="rounded-[5px] h-[17vh]"
              onChange={(e) => setQuestion(e.target.value)}
            ></textarea>
          </div>
          <div className="flex flex-col gap-[5px]">
            <label htmlFor="answer">Answer: </label>
            <div className="relative flex flex-col">
              <textarea
                id="answer"
                className="rounded-[5px] h-[17vh]"
                onChange={(e) => setAnswer(e.target.value)}
              ></textarea>
            </div>
          </div>
          <button type="submit" className="cursor-pointer">
            submit        
          </button>
        </div>
      </form>
    </div>
  )
}
export default CreateFlashCardModal