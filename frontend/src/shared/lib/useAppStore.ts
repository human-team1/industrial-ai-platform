import { create } from 'zustand'

type AppState = {
  appName: string
  setAppName: (appName: string) => void
}

export const useAppStore = create<AppState>((set) => ({
  appName: import.meta.env.VITE_APP_NAME ?? 'Industrial AI Platform',
  setAppName: (appName) => set({ appName }),
}))
