import { apiClient } from '../../../shared/api/client'
import type { SignupRequest, SignupResponse } from '../types'

// 회원가입 API
export const signup = async (data: SignupRequest): Promise<SignupResponse> => {
  const response = await apiClient.post<{ success: boolean; data: SignupResponse; message: string }>(
    '/api/auth/signup',
    data
  )
  return response.data.data
}

// Google 로그인 정보 가져오기 (구글 OAuth 후 호출)
export const getGoogleUserInfo = async (): Promise<{ email: string; name: string }> => {
  // 실제 구현에서는 Google OAuth 콜백에서 토큰을 통해 사용자 정보를 가져옵니다.
  // 여기서는 예시 데이터를 반환합니다.
  return {
    email: '',
    name: ''
  }
}
