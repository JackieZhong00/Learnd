import { create } from 'zustand'

interface createCardModalState {
  isOpen: boolean
  openModal: () => void
  closeModal: () => void
}

const useCreateCardModal = create<createCardModalState>((set) => ({
  isOpen: false,
  openModal: () => set({ isOpen: true }),
  closeModal: () => set({ isOpen: false }),
}))

export default useCreateCardModal
