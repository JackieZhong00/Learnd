import { create } from 'zustand'

interface createDeckModalState {
  isOpen: boolean
  openModal: () => void
  closeModal: () => void
}

const useCreateDeckModal = create<createDeckModalState>((set) => ({
  isOpen: false,
  openModal: () => set({ isOpen: true }),
  closeModal: () => set({ isOpen: false }),
}))

export default useCreateDeckModal
