import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthPage } from '../pages/AuthPage'
import { SignupPage } from '../pages/SignupPage'
import { SignupStatusPage } from '../pages/SignupStatusPage'
import { ChatbotPage } from '../pages/ChatbotPage'
import { DashboardPage } from '../pages/DashboardPage'
import { InspectionPage } from '../pages/InspectionPage'
import { ResultPage } from '../pages/ResultPage'
import { ResultDetailPage } from '../pages/ResultDetailPage'
import { AdminSignupRequestsPage } from '../pages/AdminSignupRequestsPage'
import { MyPage } from '../pages/MyPage'
import { UserSettingsPage } from '../pages/UserSettingsPage'
import { AppLayout } from '../shared/ui/layout/AppLayout'
import { GuestRoute, ProtectedRoute } from '../shared/ui/route/ProtectedRoute'

export function AppRouter() {
  return (
    <Routes>
      {/* 미인증 전용 — 로그인 유저는 /dashboard로 리다이렉트 */}
      <Route element={<GuestRoute />}>
        <Route path="/auth" element={<AuthPage />} />
      </Route>

      {/* 인증 없이 접근 가능한 독립 페이지 */}
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/pending" element={<SignupStatusPage status="pending" />} />
      <Route path="/rejected" element={<SignupStatusPage status="rejected" />} />

      {/* 인증 필요 — 미인증 시 /auth로 리다이렉트 */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/inspections" element={<InspectionPage />} />
          <Route path="/results" element={<ResultPage />} />
          <Route path="/results/:resultId" element={<ResultDetailPage />} />
          <Route path="/chatbot" element={<ChatbotPage />} />
          <Route path="/mypage" element={<MyPage />} />
          <Route path="/settings" element={<UserSettingsPage />} />
        </Route>

        {/* ADMIN 전용 — USER 접근 시 /dashboard로 리다이렉트 */}
        <Route element={<ProtectedRoute requiredRole="ROLE_SITE_ADMIN" />}>
          <Route element={<AppLayout />}>
            <Route path="/admin/signup-requests" element={<AdminSignupRequestsPage />} />
          </Route>
        </Route>
      </Route>
    </Routes>
  )
}
