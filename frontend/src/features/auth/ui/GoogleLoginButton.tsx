import { GoogleLogin } from '@react-oauth/google'
import { useAuthStore } from '../model'

export function GoogleLoginButton() {
  const setIdToken = useAuthStore((s) => s.setIdToken)

  return (
    <GoogleLogin
      onSuccess={(response) => {
        if (response.credential) {
          setIdToken(response.credential)
        }
      }}
      onError={() => {
        console.error('Google 로그인 실패')
      }}
    />
  )
}
