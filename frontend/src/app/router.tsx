import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthPage } from '../pages/AuthPage'
import { ChatbotPage } from '../pages/ChatbotPage'
import { DashboardPage } from '../pages/DashboardPage'
import { InspectionPage } from '../pages/InspectionPage'
import { ResultPage } from '../pages/ResultPage'
import { LoginPage } from '../pages/LoginPage' // ✅ 추가
import { AppLayout } from '../shared/ui/layout/AppLayout'

export function AppRouter() {
  return (
    <Routes>
      {/* 로그인 페이지 (레이아웃 없음) */}
      <Route path="/login" element={<LoginPage />} /> {/* ✅ 추가 */}

      {/* 메인 레이아웃 */}
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/login" replace />} /> {/* ✅ /login으로 변경 */}
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/inspections" element={<InspectionPage />} />
        <Route path="/results" element={<ResultPage />} />
        <Route path="/chatbot" element={<ChatbotPage />} />
        <Route path="/auth" element={<AuthPage />} />
      </Route>
    </Routes>
  )
}
