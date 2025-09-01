import { create } from 'zustand'

interface openTestModalState {
  isOpen: boolean
  setTrue: () => void
  setFalse: () => void
}

const useTestModalState = create<openTestModalState>((set) => ({
  isOpen: false,
  setTrue: () => set({ isOpen: true }),
  setFalse: () => set({ isOpen: false }),
}))

export default useTestModalState

