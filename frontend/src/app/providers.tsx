import { BrowserRouter } from 'react-router-dom'
import type { PropsWithChildren } from 'react'
import { GoogleOAuthProvider } from '@react-oauth/google'
import { AuthProvider } from '../features/auth/model/AuthProvider'

export function AppProviders({ children }: PropsWithChildren) {
  return (
    <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID ?? ''}>
      <BrowserRouter>
        <AuthProvider>{children}</AuthProvider>
      </BrowserRouter>
    </GoogleOAuthProvider>
  )
}
