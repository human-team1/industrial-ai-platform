import { GoogleLogin } from '@react-oauth/google'
import { useState } from 'react'
import { useGoogleLoginHandler } from '../model'

export function GoogleLoginButton() {
  const { handleCredential } = useGoogleLoginHandler()
  const [error, setError] = useState<string | null>(null)

  return (
    <div>
      <GoogleLogin
        onSuccess={(credentialResponse) => {
          if (credentialResponse.credential) {
            setError(null)
            handleCredential(credentialResponse.credential).catch(() => {
              setError('로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.')
            })
          }
        }}
        onError={() => {
          setError('Google 로그인에 실패했습니다. 잠시 후 다시 시도해주세요.')
        }}
        useOneTap={false}
      />
      {error && <p style={{ color: '#dc3545', marginTop: '8px', fontSize: '14px' }}>{error}</p>}
    </div>
  )
}
