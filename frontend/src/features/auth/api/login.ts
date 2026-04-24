import { apiClient } from '@/shared/api/client'
import type { GoogleLoginResult } from '@/features/auth/types'

export const verifyGoogleLogin = async (idToken: string): Promise<GoogleLoginResult> => {
  const response = await apiClient.post<GoogleLoginResult>('/api/v1/auth/google', { idToken })
  return response.data
}