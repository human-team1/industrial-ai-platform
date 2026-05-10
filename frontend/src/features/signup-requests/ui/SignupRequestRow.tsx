import type { SignupRequestSummary } from '../types'

type Props = {
  request: SignupRequestSummary
  processing: boolean
  onApprove: (requestId: number) => void
  onReject: (requestId: number) => void
}

export function SignupRequestRow({ request, processing, onApprove, onReject }: Props) {
  return (
    <tr style={{ borderBottom: '1px solid #eee' }}>
      <td style={{ padding: '8px' }}>
        <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          {request.picture && (
            <img
              src={request.picture}
              alt={request.name}
              style={{ width: 28, height: 28, borderRadius: '50%' }}
            />
          )}
          {request.name}
        </span>
      </td>
      <td style={{ padding: '8px' }}>{request.email}</td>
      <td style={{ padding: '8px' }}>
        {new Date(request.requestedAt).toLocaleString('ko-KR')}
      </td>
      <td style={{ padding: '8px', display: 'flex', gap: '8px' }}>
        <button
          onClick={() => onApprove(request.requestId)}
          disabled={processing}
          style={{
            padding: '4px 12px',
            background: '#2563eb',
            color: '#fff',
            border: 'none',
            borderRadius: 4,
            cursor: 'pointer',
          }}
        >
          승인
        </button>
        <button
          onClick={() => onReject(request.requestId)}
          disabled={processing}
          style={{
            padding: '4px 12px',
            background: '#dc2626',
            color: '#fff',
            border: 'none',
            borderRadius: 4,
            cursor: 'pointer',
          }}
        >
          거절
        </button>
      </td>
    </tr>
  )
}
