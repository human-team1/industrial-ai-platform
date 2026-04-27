import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../../../features/auth/model'
import type { AuthRole } from '../../../features/auth/types'

type Props = {
  requiredRole?: AuthRole
}

export function ProtectedRoute({ requiredRole }: Props) {
  const { accessToken, user, loading } = useAuth()

  if (loading) {
    return null
  }

  if (!accessToken || !user) {
    return <Navigate to="/auth" replace />
  }

  if (requiredRole && user.role !== requiredRole) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}

export function GuestRoute() {
  const { accessToken, user, loading } = useAuth()

  if (loading) {
    return null
  }

  if (accessToken && user) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}
