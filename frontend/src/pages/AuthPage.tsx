import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { SignupForm } from '../features/auth/ui'

interface GoogleUserInfo {
  email: string
  name: string
  googleSub: string
  picture: string
}

export function AuthPage() {
  const navigate = useNavigate()
  const [googleInfo, setGoogleInfo] = useState<GoogleUserInfo | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const checkAuthAndFetchInfo = () => {
      const storedGoogleInfo = localStorage.getItem('googleUserInfo')
      
      if (!storedGoogleInfo) {
        navigate('/login', { replace: true })
        return
      }

      try {
        const parsed = JSON.parse(storedGoogleInfo) as GoogleUserInfo
        if (parsed?.email && parsed?.googleSub) {
          setGoogleInfo(parsed)
        } else {
          throw new Error('Incomplete user info')
        }
      } catch (error) {
        console.error('Failed to parse googleUserInfo:', error)
        localStorage.removeItem('googleUserInfo')
        navigate('/login', { replace: true })
      } finally {
        setIsLoading(false)
      }
    }

    checkAuthAndFetchInfo()
  }, [navigate])

  if (isLoading) {
    return (
      <div className="d-flex justify-content-center align-items-center vh-100">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    )
  }

  if (!googleInfo) return null

  return (
    <section className="page-panel container py-5">
      <div className="text-center mb-4">
        <p className="text-uppercase text-secondary small mb-1">Auth</p>
        <h1 className="h3 fw-bold">회원가입 완료하기</h1>
        <p className="text-muted">추가 정보를 입력하여 회원가입을 완료해 주세요.</p>
      </div>

      <div className="row justify-content-center">
        <div className="col-md-8 col-lg-6 col-xl-5">
          <div className="card shadow-sm border-0 p-4">
            <SignupForm
              googleEmail={googleInfo.email}
              googleName={googleInfo.name}
              googleSub={googleInfo.googleSub}
              googlePicture={googleInfo.picture}
            />
          </div>
        </div>
      </div>
    </section>
  )
}