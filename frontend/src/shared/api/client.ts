import axios, { AxiosError } from 'axios'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10_000,
})

export const aiApiClient = axios.create({
  baseURL: import.meta.env.VITE_AI_API_BASE_URL,
  timeout: 30_000,
})

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
