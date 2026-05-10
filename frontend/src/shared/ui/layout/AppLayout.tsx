import { useEffect, useState } from 'react'
import { Outlet, useSearchParams } from 'react-router-dom'
import { useAuth } from '../../../features/auth/model'
import { NoticeBanner } from '../feedback/NoticeBanner'
import { AppSidebar } from './AppSidebar'
import { AppHeader } from './AppHeader'

export function AppLayout() {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)
  const { user, logout } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const denied = searchParams.get('denied') === '1'
  const [showDenied, setShowDenied] = useState(false)

  useEffect(() => {
    if (denied) {
      setShowDenied(true)
      const next = new URLSearchParams(searchParams)
      next.delete('denied')
      setSearchParams(next, { replace: true })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [denied])

  return (
    <div className="flex min-h-screen bg-[#f5f6fb]">
      {showDenied && (
        <NoticeBanner
          message="접근 권한이 없습니다."
          variant="error"
          onClose={() => setShowDenied(false)}
        />
      )}

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
