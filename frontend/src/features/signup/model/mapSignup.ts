import type { NewUserInfo } from '../../../entities/auth'

type SignupRequestPayload = {
  signupToken: string
  email: string
  name: string
  picture?: string
}

export function mapToSignupPayload(userInfo: NewUserInfo): SignupRequestPayload {
  return {
    signupToken: userInfo.signupToken,
    email: userInfo.email,
    name: userInfo.name,
    picture: userInfo.picture,
  }
}
