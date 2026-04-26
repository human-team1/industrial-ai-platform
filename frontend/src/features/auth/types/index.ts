export type AuthStatus = 'NEW' | 'PENDING' | 'REJECTED' | 'ACTIVE'

export type AuthUser = {
  userId: number
  googleSub: string
  email: string
  name: string
  picture?: string
  role: string
}

export type GoogleLoginResponse = {
  userStatus: AuthStatus
  // ACTIVE일 때만 존재
  accessToken?: string
  // refreshToken은 HttpOnly Cookie로 전달 — 프론트 접근 불가
  userId?: number
  googleSub?: string
  email?: string
  name?: string
  picture?: string
  role?: string
}

// 회원가입 페이지로 전달할 Google 사용자 정보
export type NewUserInfo = {
  googleSub: string
  email: string
  name: string
  picture?: string
}
