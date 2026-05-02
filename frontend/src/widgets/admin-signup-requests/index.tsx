import { useAdminSignupRequests } from '../../features/signup-requests/model/useAdminSignupRequests'
import {
  SignupRequestStateBox,
  SignupRequestTable,
} from '../../features/signup-requests/ui'

export function AdminSignupRequestsWidget() {
  const { requests, loading, error, processingId, approve, reject } = useAdminSignupRequests()

  if (loading || error) {
    return <SignupRequestStateBox loading={loading} error={error} />
  }

  return (
    <div>
      <h2>가입 신청 관리</h2>
      <SignupRequestTable
        requests={requests}
        processingId={processingId}
        onApprove={approve}
        onReject={reject}
      />
    </div>
  )
}
