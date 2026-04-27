import type { PropsWithChildren } from 'react'
import { AuthContext, useAuthState } from './index'

export function AuthProvider({ children }: PropsWithChildren) {
  const authState = useAuthState()

  // refresh 완료 전까지 라우터 렌더링 차단 — 미인증 리다이렉트 오탐 방지
  if (authState.loading) {
    return null
  }

  return <AuthContext.Provider value={authState}>{children}</AuthContext.Provider>
}