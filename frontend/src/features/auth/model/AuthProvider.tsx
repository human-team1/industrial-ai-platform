import type { PropsWithChildren } from 'react'
import { AuthContext, useAuthState } from './index'

export function AuthProvider({ children }: PropsWithChildren) {
  const authState = useAuthState()
  return <AuthContext.Provider value={authState}>{children}</AuthContext.Provider>
}
