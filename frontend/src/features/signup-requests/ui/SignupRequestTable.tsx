import type { SignupRequestSummary } from '../types'
import { SignupRequestEmptyState } from './SignupRequestEmptyState'
import { SignupRequestRow } from './SignupRequestRow'

type Props = {
  requests: SignupRequestSummary[]
  processingId: number | null
  onApprove: (requestId: number) => void
  onReject: (requestId: number) => void
}

export function SignupRequestTable({ requests, processingId, onApprove, onReject }: Props) {
  if (requests.length === 0) {
    return <SignupRequestEmptyState />
  }

  return (
    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
      <thead>
        <tr style={{ borderBottom: '1px solid #ddd', textAlign: 'left' }}>
          <th style={{ padding: '8px' }}>이름</th>
          <th style={{ padding: '8px' }}>이메일</th>
          <th style={{ padding: '8px' }}>신청 일시</th>
          <th style={{ padding: '8px' }}>처리</th>
        </tr>
      </thead>
      <tbody>
        {requests.map((req) => (
          <SignupRequestRow
            key={req.requestId}
            request={req}
            processing={processingId === req.requestId}
            onApprove={onApprove}
            onReject={onReject}
          />
        ))}
      </tbody>
    </table>
  )
}
