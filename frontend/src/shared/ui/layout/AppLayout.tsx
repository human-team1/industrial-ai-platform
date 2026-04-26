import { NavLink, Outlet } from 'react-router-dom'

const navItems = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/inspections', label: 'Inspection' },
  { to: '/results', label: 'Results' },
  { to: '/chatbot', label: 'RAG Chat' },
  { to: '/auth', label: 'Auth' },
]

export function AppLayout() {
  return (
    <div className="app-shell">
      <nav className="navbar">
        <div className="container">
          <NavLink className="navbar-brand" to="/dashboard">
            Industrial AI Platform
          </NavLink>
          
          <div className="navbar-nav">
            {navItems.map((item) => (
              <NavLink 
                key={item.to} 
                className="nav-link" 
                to={item.to}
              >
                {item.label}
              </NavLink>
            ))}
          </div>
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