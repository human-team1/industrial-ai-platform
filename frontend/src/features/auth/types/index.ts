// 회원가입 요청 타입
export interface SignupRequest {
  email: string
  name: string
  googleSub: string
  picture?: string
  company: string
  department?: string
  position?: string
  phone: string
}

// 회원가입 응답 타입
export interface SignupResponse {
  id: number
  email: string
  name: string
  picture?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
}

// Google 정보 타입
export interface GoogleUserInfo {
  email: string
  name: string
  googleSub: string
  picture?: string
}

// 폼 데이터 타입 (2단계)
export interface SignupFormData {
  email: string
  name: string
  phone: string
  company: string
  department: string
  position: string
}

// 폼 에러 타입
export interface SignupFormErrors {
  email?: string
  name?: string
  phone?: string
  company?: string
  department?: string
  position?: string
}

// 회원가입 단계
export type SignupStep = 1 | 2 | 3