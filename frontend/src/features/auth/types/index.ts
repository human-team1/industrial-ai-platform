export type UserStatus = 'ACTIVE' | 'PENDING' | 'REJECTED' | 'NOT_REGISTERED'

export type AuthUser = {
  id: number
  email: string
  name: string
  picture?: string
  role: string
}

export type GoogleLoginResult = {
  status: UserStatus
  accessToken?: string
  user?: AuthUser
}
