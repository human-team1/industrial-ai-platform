import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../../../features/auth/model'
import type { AuthRole } from '../../../features/auth/types'

export function RequireAuth() {
  const { accessToken, user, loading } = useAuth()

  if (loading) return null

  if (!accessToken || !user) {
    return <Navigate to="/auth" replace />
  }

  return <Outlet />
}

export function RequireActiveUser() {
  const { user, loading } = useAuth()

  if (loading) return null
  if (!user) return <Navigate to="/auth" replace />

  switch (user.status) {
    case 'PENDING':
      return <Navigate to="/pending" replace />
    case 'REJECTED':
      return <Navigate to="/rejected" replace />
    case 'INACTIVE':
      return <Navigate to="/auth?inactive=1" replace />
    case 'ACTIVE':
    default:
      return <Outlet />
  }
}

type RequireRoleProps = {
  roles: AuthRole[]
}

export function RequireRole({ roles }: RequireRoleProps) {
  const { user, loading } = useAuth()

  if (loading) return null
  if (!user) return <Navigate to="/auth" replace />

  if (!roles.includes(user.role)) {
    return <Navigate to="/dashboard?denied=1" replace />
  }

  return <Outlet />
}

export function GuestRoute() {
  const { accessToken, user, loading } = useAuth()

  if (loading) return null

  if (accessToken && user) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}

type ProtectedRouteProps = {
  requiredRole?: AuthRole
}

export function ProtectedRoute({ requiredRole }: ProtectedRouteProps) {
  const { accessToken, user, loading } = useAuth()

  if (loading) return null
  if (!accessToken || !user) return <Navigate to="/auth" replace />
  if (requiredRole && user.role !== requiredRole) {
    return <Navigate to="/dashboard?denied=1" replace />
  }
  return <Outlet />
}
