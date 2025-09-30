import toast, { Toaster } from 'react-hot-toast'
import useEditCategoryModal from '../../hooks/useEditCategoryModal'
import axios from "axios"
import { GraphNode } from '../DeckHome'
import  {useState} from 'react'
type CategoryModalProps = {
    id : number | undefined;
    name: string | undefined;
}

const EditCategoryModal = ({id, name} : CategoryModalProps) => {
  const [categoryName, setCategoryName] = useState<string>(name || "")
  const editCategoryModalHook = useEditCategoryModal()
  const handleSubmit = async () => {
    //make api call to change category name
    try {
        await axios.patch(`http://localhost:8080/api/category/update_name/${id}`, {name:categoryName}, {withCredentials: true})
        toast.success("Category name updated successfully")
        editCategoryModalHook.closeModal()
    } catch (error) {
        console.log("error updating category name: " + error)
    }
  }
  const handleDeleteCategory = async () => {
    try {
      await axios.delete(
        `http://localhost:8080/api/category/${id}`, { withCredentials: true }
      )
      toast.success('Category deleted successfully')
      editCategoryModalHook.closeModal()
    } catch (error) {
      console.log('error deleting category: ' + error)
    }
  }
  

  return (
    <div className="bg-[#D9D9D9] w-[33vw] h-[20vh] border border-gray-300 border-2 rounded-[35px]">
      <Toaster position="top-center" />
      <form
        onSubmit={handleSubmit}
        className="flex flex-col gap-[5px] ml-[10px] mr-[10px]"
      >
        <div className="flex justify-between items-center">
          <p className="text-[20px] underline">Edit Category: </p>

          <button
            className="underline mr-[10px]"
            onClick={() => {
              editCategoryModalHook.closeModal()
            }}
          >
            close
          </button>
        </div>
        <div className="flex flex-col gap-[25px]">
          <div className="flex flex-col gap-[5px]">
            <label htmlFor="categoryName">Category Name</label>

            <input
              id="categoryName"
              value={categoryName}
              className="rounded-[25px]"
              onChange={(e) => setCategoryName(e.target.value)}
            ></input>
          </div>
        </div>
        <div className="flex justify-between">
          <button onClick={handleSubmit} className="cursor-pointer w-[8vw]">
            Submit
          </button>
          <button
            className="flex w-[2vw] h-[3vh] cursor-pointer bg-[#f06e7b]"
            onClick={handleDeleteCategory}
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
        </div>
      </form>
    </div>
  )
}
export default EditCategoryModal
