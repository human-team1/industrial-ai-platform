import axios, { AxiosError } from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

// accessToken을 메모리에만 보관 (localStorage 저장 금지 — XSS 탈취 방지)
let _memoryToken: string | null = null

export function setMemoryToken(token: string | null) {
  _memoryToken = token
}

export function getMemoryToken(): string | null {
  return _memoryToken
}

export const apiClient = axios.create({
  baseURL: BASE_URL,
  // timeout: 10_000,
  timeout: 30000_000,
  withCredentials: true,
})

// 토큰 갱신 전용 인스턴스 — 인터셉터 없음 (무한루프 방지)
export const refreshAxios = axios.create({
  baseURL: BASE_URL,
  timeout: 10_000,
  withCredentials: true,
})

// 공개 엔드포인트 전용 인스턴스 — 인증 헤더/리다이렉트 인터셉터 없음
export const publicAxios = axios.create({
  baseURL: BASE_URL,
  timeout: 10_000,
})

// Request: 메모리의 accessToken을 Authorization 헤더에 자동 첨부
apiClient.interceptors.request.use((config) => {
  const token = getMemoryToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response: 401 → refresh → 원본 요청 재시도
let isRefreshing = false
let pendingQueue: Array<{ resolve: (token: string) => void; reject: (err: unknown) => void }> = []

function processQueue(error: unknown, token: string | null) {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token!)
  })
  pendingQueue = []
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as typeof error.config & { _retry?: boolean }

    if (error.response?.status === 403) {
      if (window.location.pathname.startsWith('/admin')) {
        window.location.href = '/dashboard'
      }
      return Promise.reject(error)
    }

    if (error.response?.status !== 401 || original?._retry) {
      return Promise.reject(error)
    }

    if (isRefreshing) {
      return new Promise<string>((resolve, reject) => {
        pendingQueue.push({ resolve, reject })
      }).then((newToken) => {
        return apiClient({
          ...original!,
          headers: { ...original!.headers, Authorization: `Bearer ${newToken}` },
        })
      })
    }

    original!._retry = true
    isRefreshing = true

    try {
      const res = await refreshAxios.post<{ success: boolean; data: { accessToken: string } }>(
        '/auth/refresh',
      )
      const newToken = res.data.data.accessToken
      setMemoryToken(newToken)
      processQueue(null, newToken)
      return apiClient({
        ...original!,
        headers: { ...original!.headers, Authorization: `Bearer ${newToken}` },
      })
    } catch (refreshError) {
      processQueue(refreshError, null)
      setMemoryToken(null)
      localStorage.removeItem('authUser')
      window.location.href = '/auth'
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  },
)

export type ApiResult<T> = {
  success: boolean
  data?: T
  message?: string
}

export function normalizeApiError(error: unknown): Error {
  if (error instanceof AxiosError) {
    const detail = error.response?.data?.detail ?? error.message
    return new Error(String(detail))
  }

  if (error instanceof Error) {
    return error
  }

  return new Error('Unexpected API error')
}
