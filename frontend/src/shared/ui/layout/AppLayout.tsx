import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { useAuth } from '../../../features/auth/model'
import { AppSidebar } from './AppSidebar'
import { AppHeader } from './AppHeader'

export function AppLayout() {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)
  const { user, logout } = useAuth()

  return (
    <div className="flex min-h-screen bg-[#f5f6fb]">
      <AppSidebar
        isCollapsed={sidebarCollapsed}
        onCollapse={() => setSidebarCollapsed(true)}
        onExpand={() => setSidebarCollapsed(false)}
      />

      <div className="flex-1 flex flex-col min-h-screen">
        <AppHeader
          userName={user?.name ?? ''}
          userRole={user?.role}
          picture={user?.picture}
          onLogout={logout}
        />

        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}