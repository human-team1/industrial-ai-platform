export type AuthStatus = 'NEW' | 'PENDING' | 'REJECTED' | 'ACTIVE'

export type AuthUser = {
  userId: number
  googleSub: string
  email: string
  name: string
  picture?: string
  phone?: string
  role: string
  organizationId?: number
  status?: string
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
  organizationId?: number
  // NEW일 때만 존재 — googleSub은 이 토큰 안에 포함
  signupToken?: string
}

// 회원가입 페이지로 전달할 정보 (display용 + signupToken)
export type NewUserInfo = {
  signupToken: string  // 서버 발급 JWT, googleSub 포함
  email: string
  name: string
  picture?: string
}

// GET /auth/me 응답 — 인증 검증 + 컨텍스트 획득용
export type AuthMeResponse = {
  userId: number
  name: string
  role: string
  status: string
  organizationId?: number
}

// GET /users/me 응답 — 전체 프로필용
export type UserMeResponse = {
  userId: number
  email: string
  name: string
  picture?: string
  phone?: string
  role: string
  organizationId?: number
  status: string
  createdAt: string
}