import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthPage } from '../pages/AuthPage'
import { SignupPage } from '../pages/SignupPage'
import { PendingPage } from '../pages/PendingPage'
import { RejectedPage } from '../pages/RejectedPage'
import { ChatbotPage } from '../pages/ChatbotPage'
import { DashboardPage } from '../pages/DashboardPage'
import { InspectionPage } from '../pages/InspectionPage'
import { ResultPage } from '../pages/ResultPage'
import { AdminSignupRequestsPage } from '../pages/AdminSignupRequestsPage'
import { AppLayout } from '../shared/ui/layout/AppLayout'

export function AppRouter() {
  return (
    <Routes>
      {/* 인증 없이 접근 가능한 독립 페이지 */}
      <Route path="/auth" element={<AuthPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/pending" element={<PendingPage />} />
      <Route path="/rejected" element={<RejectedPage />} />

      {/* AppLayout이 적용되는 서비스 페이지 */}
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/inspections" element={<InspectionPage />} />
        <Route path="/results" element={<ResultPage />} />
        <Route path="/chatbot" element={<ChatbotPage />} />
        <Route path="/admin/signup-requests" element={<AdminSignupRequestsPage />} />
      </Route>
    </Routes>
  )
}
