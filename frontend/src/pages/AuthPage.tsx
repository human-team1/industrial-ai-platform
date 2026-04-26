import { GoogleLoginButton } from '../features/auth/ui/GoogleLoginButton'

export function AuthPage() {
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
        <h1 style={{ fontSize: '22px', fontWeight: 700, marginBottom: '8px' }}>
          Industrial AI Platform
        </h1>
        <p style={{ color: '#6c757d', fontSize: '14px', marginBottom: '32px' }}>
          Google 계정으로 로그인하세요
        </p>
        <div style={{ display: 'flex', justifyContent: 'center' }}>
          <GoogleLoginButton />
        </div>
      </div>
    </div>
  )
}
