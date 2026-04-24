import { create } from 'zustand'

type AuthState = {
  idToken: string | null
  setIdToken: (token: string) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  idToken: null,
  setIdToken: (token) => set({ idToken: token }),
  clearAuth: () => set({ idToken: null }),
}))
