import { createContext, useContext, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { postGoogleLogin, postLogout } from '../api'
import type { AuthUser, NewUserInfo } from '../types'

const ACCESS_TOKEN_KEY = 'accessToken'
const USER_KEY = 'authUser'

export type AuthState = {
  accessToken: string | null
  user: AuthUser | null
  setAuth: (token: string, user: AuthUser) => void
  logout: () => void
}

export const AuthContext = createContext<AuthState>({
  accessToken: null,
  user: null,
  setAuth: () => {},
  logout: () => {},
})

export function useAuthState(): AuthState {
  const [accessToken, setAccessToken] = useState<string | null>(
    () => localStorage.getItem(ACCESS_TOKEN_KEY),
  )
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = localStorage.getItem(USER_KEY)
    return stored ? (JSON.parse(stored) as AuthUser) : null
  })

  const setAuth = useCallback((token: string, authUser: AuthUser) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(authUser))
    setAccessToken(token)
    setUser(authUser)
  }, [])

  const logout = useCallback(() => {
    postLogout().finally(() => {
      localStorage.removeItem(ACCESS_TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
      window.location.href = '/auth'
    })
  }, [])

  return { accessToken, user, setAuth, logout }
}

export function useAuth() {
  return useContext(AuthContext)
}

export function useGoogleLoginHandler() {
  const { setAuth } = useAuth()
  const navigate = useNavigate()

  const handleCredential = useCallback(
    async (credential: string) => {
      const result = await postGoogleLogin(credential)

      switch (result.userStatus) {
        case 'ACTIVE':
          setAuth(result.accessToken!, {
            userId: result.userId!,
            googleSub: result.googleSub!,
            email: result.email!,
            name: result.name!,
            picture: result.picture,
            role: result.role!,
          })
          navigate('/dashboard')
          break

        case 'NEW': {
          const newUserInfo: NewUserInfo = {
            googleSub: result.googleSub!,
            email: result.email!,
            name: result.name!,
            picture: result.picture,
          }
          navigate('/signup', { state: newUserInfo })
          break
        }

        case 'PENDING':
          navigate('/pending')
          break

        case 'REJECTED':
          navigate('/rejected')
          break
      }
    },
    [setAuth, navigate],
  )

  return { handleCredential }
}
