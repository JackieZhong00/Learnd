import { create } from 'zustand'

interface createEditCategoryModalState {
  isOpen: boolean
  openModal: () => void
  closeModal: () => void
}

const useEditCategoryModal = create<createEditCategoryModalState>((set) => ({
  isOpen: false,
  openModal: () => set({ isOpen: true }),
  closeModal: () => set({ isOpen: false }),
}))

export default useEditCategoryModal
