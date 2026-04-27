import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { postGoogleLogin, postLogout, postRefreshToken } from '../api'
import { setMemoryToken } from '../../../shared/api/client'
import type { AuthUser, NewUserInfo } from '../types'

const USER_KEY = 'authUser'

export type AuthState = {
  accessToken: string | null
  user: AuthUser | null
  loading: boolean
  setAuth: (token: string, user: AuthUser) => void
  logout: () => void
}

export const AuthContext = createContext<AuthState>({
  accessToken: null,
  user: null,
  loading: true,
  setAuth: () => {},
  logout: () => {},
})

export function useAuthState(): AuthState {
  // accessToken은 메모리(state)에만 보관 — localStorage 저장 금지
  const [accessToken, setAccessToken] = useState<string | null>(null)
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = localStorage.getItem(USER_KEY)
    return stored ? (JSON.parse(stored) as AuthUser) : null
  })
  const [loading, setLoading] = useState(true)

  // 앱 초기화: localStorage에 user가 있으면 refresh 쿠키로 accessToken 복구
  // cancelled 플래그로 StrictMode 이중 호출 방지
  useEffect(() => {
    let cancelled = false

    const storedUser = localStorage.getItem(USER_KEY)
    if (!storedUser) {
      setLoading(false)
      return
    }

    postRefreshToken()
      .then(({ accessToken: newToken }) => {
        if (cancelled) return
        setMemoryToken(newToken)
        setAccessToken(newToken)
      })
      .catch(() => {
        if (cancelled) return
        setMemoryToken(null)
        localStorage.removeItem(USER_KEY)
        setUser(null)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const setAuth = useCallback((token: string, authUser: AuthUser) => {
    setMemoryToken(token)
    localStorage.setItem(USER_KEY, JSON.stringify(authUser))
    setAccessToken(token)
    setUser(authUser)
  }, [])

  const logout = useCallback(() => {
    postLogout().finally(() => {
      setMemoryToken(null)
      localStorage.removeItem(USER_KEY)
      setAccessToken(null)
      setUser(null)
      window.location.href = '/auth'
    })
  }, [])

  return { accessToken, user, loading, setAuth, logout }
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
            organizationId: result.organizationId,
          })
          navigate('/dashboard')
          break

        case 'NEW': {
          const newUserInfo: NewUserInfo = {
            signupToken: result.signupToken!,
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