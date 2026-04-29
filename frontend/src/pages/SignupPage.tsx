import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import type { NewUserInfo } from '../entities/auth'
import { AppHeader } from '../shared/ui/layout/AppHeader'
import { AppSidebar } from '../shared/ui/layout/AppSidebar'
import { SignupFormSection } from '../widgets/signup-form/ui/SignupFormSection'

export function SignupPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const userInfo = location.state as NewUserInfo | null

  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)

  if (!userInfo?.signupToken) {
    return <Navigate to="/auth" replace />
  }

  return (
    <div className="flex min-h-screen bg-[#fbfcfe] min-w-[1342px]">
      <AppSidebar
        isCollapsed={sidebarCollapsed}
        onCollapse={() => setSidebarCollapsed(true)}
        onExpand={() => setSidebarCollapsed(false)}
      />

      <div className="flex-1 flex flex-col min-h-screen">
        <AppHeader userName={userInfo.name} userRole="신규 사용자" />

        <div className="flex flex-1 min-h-0">
          <SignupFormSection
            userInfo={userInfo}
            onBack={() => navigate('/auth')}
            onSubmitted={(result) => {
              navigate(result.status === 'REJECTED' ? '/rejected' : '/pending', {
                replace: true,
              })
            }}
          />
        </div>
      </div>
    </div>
  )
}
