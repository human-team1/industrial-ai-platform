import { useAuthStore } from '@/features/auth/model'
import { GoogleLoginButton } from '@/features/auth/ui'

export function AuthPage() {
  const idToken = useAuthStore((s) => s.idToken)

  return (
    <section className="page-panel py-5">
      <p className="text-uppercase text-secondary small mb-2">Auth</p>
      <h1 className="h3 mb-4">로그인</h1>

      {idToken ? (
        <div className="alert alert-success" role="alert">
          <strong>Google 로그인 성공</strong>
          <p className="mb-1 mt-2 small text-muted">id_token 수신됨:</p>
          <code className="d-block text-break small">{idToken.slice(0, 80)}…</code>
        </div>
      ) : (
        <div className="d-flex flex-column align-items-start gap-3">
          <p className="text-muted mb-0">Google 계정으로 로그인하세요.</p>
          <GoogleLoginButton />
        </div>
      )}
    </section>
  )
}
