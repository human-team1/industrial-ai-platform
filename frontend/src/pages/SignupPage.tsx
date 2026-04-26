import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import type { NewUserInfo } from '../features/auth/types'
import { postSignupRequest } from '../features/auth/api'

export function SignupPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const userInfo = location.state as NewUserInfo | null

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!userInfo) {
    navigate('/auth', { replace: true })
    return null
  }

  async function handleSubmit() {
    setLoading(true)
    setError(null)
    try {
      await postSignupRequest(userInfo!)
      navigate('/pending', { replace: true })
    } catch {
      setError('가입 신청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#f8f9fa',
      }}
    >
      <div
        style={{
          backgroundColor: '#fff',
          borderRadius: '12px',
          padding: '48px 40px',
          boxShadow: '0 4px 24px rgba(0,0,0,0.08)',
          width: '100%',
          maxWidth: '480px',
        }}
      >
        <h2 style={{ fontSize: '20px', fontWeight: 700, marginBottom: '8px' }}>회원가입 신청</h2>
        <p style={{ color: '#6c757d', fontSize: '14px', marginBottom: '28px' }}>
          아래 정보를 확인하고 가입을 신청하세요. 관리자 승인 후 서비스를 이용할 수 있습니다.
        </p>

        {userInfo.picture && (
          <div style={{ textAlign: 'center', marginBottom: '20px' }}>
            <img
              src={userInfo.picture}
              alt="프로필"
              style={{ width: '64px', height: '64px', borderRadius: '50%' }}
            />
          </div>
        )}

        <div style={{ marginBottom: '16px' }}>
          <label style={{ display: 'block', fontSize: '13px', color: '#495057', marginBottom: '4px' }}>
            이름
          </label>
          <input
            type="text"
            defaultValue={userInfo.name}
            readOnly
            style={{
              width: '100%',
              padding: '10px 12px',
              border: '1px solid #dee2e6',
              borderRadius: '6px',
              fontSize: '14px',
              backgroundColor: '#f8f9fa',
              boxSizing: 'border-box',
            }}
          />
        </div>

        <div style={{ marginBottom: '28px' }}>
          <label style={{ display: 'block', fontSize: '13px', color: '#495057', marginBottom: '4px' }}>
            이메일
          </label>
          <input
            type="email"
            defaultValue={userInfo.email}
            readOnly
            style={{
              width: '100%',
              padding: '10px 12px',
              border: '1px solid #dee2e6',
              borderRadius: '6px',
              fontSize: '14px',
              backgroundColor: '#f8f9fa',
              boxSizing: 'border-box',
            }}
          />
        </div>

        {error && (
          <p style={{ color: '#dc3545', fontSize: '14px', marginBottom: '12px' }}>{error}</p>
        )}

        <button
          onClick={handleSubmit}
          disabled={loading}
          style={{
            width: '100%',
            padding: '12px',
            backgroundColor: loading ? '#6c757d' : '#0d6efd',
            color: '#fff',
            border: 'none',
            borderRadius: '6px',
            fontSize: '15px',
            fontWeight: 600,
            cursor: loading ? 'not-allowed' : 'pointer',
          }}
        >
          {loading ? '신청 중...' : '가입 신청하기'}
        </button>

        <button
          onClick={() => navigate('/auth')}
          disabled={loading}
          style={{
            width: '100%',
            marginTop: '12px',
            padding: '10px',
            backgroundColor: 'transparent',
            color: '#6c757d',
            border: '1px solid #dee2e6',
            borderRadius: '6px',
            fontSize: '14px',
            cursor: loading ? 'not-allowed' : 'pointer',
          }}
        >
          돌아가기
        </button>
      </div>
    </div>
  )
}
