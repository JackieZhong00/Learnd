import { create } from 'zustand'

interface createIsLoggedOut {
  isLoggedOut: boolean
  setTrue: () => void
  setFalse: () => void
}

const useIsLoggedOut = create<createIsLoggedOut>((set) => ({
  isLoggedOut: false,
  setTrue: () => set({ isLoggedOut: true }),
  setFalse: () => set({ isLoggedOut: false }),
}))

export default useIsLoggedOut

