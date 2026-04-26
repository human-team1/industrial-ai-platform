import { useNavigate } from 'react-router-dom'

export function PendingPage() {
  const navigate = useNavigate()

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
          maxWidth: '400px',
          textAlign: 'center',
        }}
      >
        <div style={{ fontSize: '48px', marginBottom: '16px' }}>⏳</div>
        <h2 style={{ fontSize: '20px', fontWeight: 700, marginBottom: '12px' }}>가입 승인 대기 중</h2>
        <p style={{ color: '#6c757d', fontSize: '14px', lineHeight: 1.6, marginBottom: '32px' }}>
          가입 신청이 접수되었습니다.
          <br />
          관리자 승인 후 서비스를 이용하실 수 있습니다.
          <br />
          승인 완료 시 등록하신 이메일로 안내드립니다.
        </p>
        <button
          onClick={() => navigate('/auth')}
          style={{
            padding: '10px 24px',
            backgroundColor: 'transparent',
            color: '#6c757d',
            border: '1px solid #dee2e6',
            borderRadius: '6px',
            fontSize: '14px',
            cursor: 'pointer',
          }}
        >
          로그인 화면으로
        </button>
      </div>
    </div>
  )
}
