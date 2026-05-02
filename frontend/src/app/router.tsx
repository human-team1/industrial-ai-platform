import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthPage } from '../pages/AuthPage'
import { SignupPage } from '../pages/SignupPage'
import { SignupStatusPage } from '../pages/SignupStatusPage'
import { ChatbotPage } from '../pages/ChatbotPage'
import { ChatbotHistoryPage } from '../pages/ChatbotHistoryPage'
import { DashboardPage } from '../pages/DashboardPage'
import { InspectionPage } from '../pages/InspectionPage'
import { UploadInspectionPage } from '../pages/UploadInspectionPage'
import { ResultPage } from '../pages/ResultPage'
import { ResultDetailPage } from '../pages/ResultDetailPage'
import { NotificationPage } from '../pages/NotificationPage'
import { AdminSignupRequestsPage } from '../pages/AdminSignupRequestsPage'
import { MyPage } from '../pages/MyPage'
import { UserSettingsPage } from '../pages/UserSettingsPage'
import { DocumentListPage } from '../pages/DocumentListPage'
import { DocumentFormPage } from '../pages/documents/DocumentFormPage'
import { AppLayout } from '../shared/ui/layout/AppLayout'
import {
  GuestRoute,
  RequireActiveUser,
  RequireAuth,
  RequireRole,
} from '../shared/ui/route/ProtectedRoute'

export function AppRouter() {
  return (
    <Routes>
      <Route element={<GuestRoute />}>
        <Route path="/auth" element={<AuthPage />} />
      </Route>

      <Route path="/signup" element={<SignupPage />} />
      <Route path="/pending" element={<SignupStatusPage status="pending" />} />
      <Route path="/rejected" element={<SignupStatusPage status="rejected" />} />

      <Route element={<RequireAuth />}>
        <Route element={<RequireActiveUser />}>
          <Route element={<AppLayout />}>
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/inspections" element={<InspectionPage />} />
            <Route path="/inspections/upload" element={<UploadInspectionPage />} />
            <Route path="/results" element={<ResultPage />} />
            <Route path="/results/:resultId" element={<ResultDetailPage />} />
            <Route path="/notifications" element={<NotificationPage />} />
            <Route path="/documents" element={<DocumentListPage />} />
            <Route path="/documents/new" element={<DocumentFormPage />} />
            <Route path="/documents/:documentId/edit" element={<DocumentFormPage />} />
            <Route path="/chatbot" element={<ChatbotPage />} />
            <Route path="/chatbot/history" element={<ChatbotHistoryPage />} />
            <Route path="/mypage" element={<MyPage />} />
            <Route path="/settings" element={<UserSettingsPage />} />
          </Route>

          <Route element={<RequireRole roles={['ROLE_SITE_ADMIN']} />}>
            <Route element={<AppLayout />}>
              <Route path="/admin/signup-requests" element={<AdminSignupRequestsPage />} />
            </Route>
          </Route>
        </Route>
      </Route>
    </Routes>
  )
}
