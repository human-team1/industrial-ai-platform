import axios, { AxiosError } from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL

export const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 10_000,
  withCredentials: true,
})

export const aiApiClient = axios.create({
  baseURL: import.meta.env.VITE_AI_API_BASE_URL,
  timeout: 30_000,
})

// 토큰 갱신 전용 인스턴스 — 인터셉터 없음 (무한루프 방지)
const refreshAxios = axios.create({
  baseURL: BASE_URL,
  timeout: 10_000,
  withCredentials: true,
})

// Request: localStorage의 accessToken을 Authorization 헤더에 자동 첨부
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
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
      localStorage.setItem('accessToken', newToken)
      processQueue(null, newToken)
      return apiClient({
        ...original!,
        headers: { ...original!.headers, Authorization: `Bearer ${newToken}` },
      })
    } catch (refreshError) {
      processQueue(refreshError, null)
      localStorage.removeItem('accessToken')
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
