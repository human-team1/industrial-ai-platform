import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../../features/auth/model'

const navItems = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/inspections', label: 'Inspection' },
  { to: '/results', label: 'Results' },
  { to: '/chatbot', label: 'RAG Chat' },
  { to: '/auth', label: 'Auth' },
]

export function AppLayout() {
  const { user, logout } = useAuth()

  return (
    <div className="app-shell">
      <nav className="navbar">
        <div className="container">
          <NavLink className="navbar-brand" to="/dashboard">
            Industrial AI Platform
          </NavLink>

          <div className="navbar-nav">
            {navItems.map((item) => (
              <NavLink key={item.to} className="nav-link" to={item.to}>
                {item.label}
              </NavLink>
            ))}
            {user?.role === 'ADMIN' && (
              <NavLink className="nav-link" to="/admin/signup-requests">
                가입 신청 관리
              </NavLink>
            )}
          </div>

          <button
            className="nav-link"
            onClick={logout}
            style={{ background: 'none', border: 'none', cursor: 'pointer' }}
          >
            로그아웃
          </button>
        </div>
      </nav>

      <main className="app-main">
        <div className="container">
          <Outlet />
        </div>
      </main>
    </div>
  )
}