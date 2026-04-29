import type { AuthRole, AuthStatus } from '../../auth/types'

// GET /users/me 응답 — view 슬라이스 전용
export type UserMeResponse = {
  userId: number
  email: string
  name: string
  picture?: string
  phone?: string
  role: AuthRole
  organizationId?: number
  organizationName?: string
  status: AuthStatus
  lastLoginAt?: string
  createdAt: string
}
