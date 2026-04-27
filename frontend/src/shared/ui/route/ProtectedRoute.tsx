import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../../../features/auth/model'

type Props = {
  requiredRole?: string
}

export function ProtectedRoute({ requiredRole }: Props) {
  const { accessToken, user } = useAuth()

  if (!accessToken || !user) {
    return <Navigate to="/auth" replace />
  }

  if (requiredRole && user.role !== requiredRole) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}

export function GuestRoute() {
  const { accessToken, user } = useAuth()

  if (accessToken && user) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}