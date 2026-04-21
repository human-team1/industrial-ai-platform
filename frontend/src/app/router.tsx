import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthPage } from '../pages/AuthPage'
import { ChatbotPage } from '../pages/ChatbotPage'
import { DashboardPage } from '../pages/DashboardPage'
import { InspectionPage } from '../pages/InspectionPage'
import { ResultPage } from '../pages/ResultPage'
import { AppLayout } from '../shared/ui/layout/AppLayout'

export function AppRouter() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/inspections" element={<InspectionPage />} />
        <Route path="/results" element={<ResultPage />} />
        <Route path="/chatbot" element={<ChatbotPage />} />
        <Route path="/auth" element={<AuthPage />} />
      </Route>
    </Routes>
  )
}
