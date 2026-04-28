import { useNavigate } from 'react-router-dom'

export function LoginPage() {
  const navigate = useNavigate()

  const handleGoogleLogin = () => {
    // TODO: 실제 Google OAuth 연동 시 수정
    // 임시로 테스트용 데이터 저장
    const mockGoogleInfo = {
      email: 'test@gmail.com',
      name: '홍길동',
      googleSub: 'google-sub-123',
      picture: ''
    }
    localStorage.setItem('googleUserInfo', JSON.stringify(mockGoogleInfo))
    navigate('/auth', { replace: true })
  }

  return (
    <div className="d-flex justify-content-center align-items-center vh-100 bg-light">
      <div className="card shadow-sm border-0 p-5" style={{ maxWidth: '420px', width: '100%' }}>
        <div className="text-center mb-4">
          <h1 className="h4 fw-bold mb-1">산업 이상 탐지 시스템</h1>
          <p className="text-muted small">안전하고 신뢰할 수 있는 모니터링</p>
        </div>

        <div className="d-grid mb-3">
          <button
            className="btn btn-outline-secondary btn-lg d-flex align-items-center justify-content-center gap-2"
            onClick={handleGoogleLogin}
          >
            <svg width="20" height="20" viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              />
            </svg>
            Google로 로그인
          </button>
        </div>

        <p className="text-center text-muted small mt-3">
          최초 로그인 시 추가 정보 입력 후 가입이 완료됩니다.
        </p>

        <hr className="my-3" />

        <p className="text-center small text-muted mb-0">
          © 2025 Industrial Anomaly Detection System. All rights reserved.
        </p>
      </div>
    </div>
  )
}
