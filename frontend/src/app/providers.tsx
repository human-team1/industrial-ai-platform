import { BrowserRouter } from 'react-router-dom'
import { GoogleOAuthProvider } from '@react-oauth/google'
import type { PropsWithChildren } from 'react'

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID as string

export function AppProviders({ children }: PropsWithChildren) {
  return (
    <GoogleOAuthProvider clientId={GOOGLE_CLIENT_ID}>
      <BrowserRouter>{children}</BrowserRouter>
    </GoogleOAuthProvider>
  )
}
