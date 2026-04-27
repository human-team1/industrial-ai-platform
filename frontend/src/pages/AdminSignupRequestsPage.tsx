import { useEffect, useState } from 'react'
import {
  getSignupRequests,
  approveSignup,
  rejectSignup,
  type SignupRequestSummary,
} from '../features/admin/api'

export function AdminSignupRequestsPage() {
  const [requests, setRequests] = useState<SignupRequestSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [processingId, setProcessingId] = useState<number | null>(null)

  useEffect(() => {
    fetchRequests()
  }, [])

  async function fetchRequests() {
    try {
      setLoading(true)
      setError(null)
      const data = await getSignupRequests()
      setRequests(data)
    } catch {
      setError('가입 신청 목록을 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }

  async function handleApprove(requestId: number) {
    setProcessingId(requestId)
    try {
      await approveSignup(requestId)
      setRequests((prev) => prev.filter((r) => r.requestId !== requestId))
    } catch {
      alert('승인 처리 중 오류가 발생했습니다.')
    } finally {
      setProcessingId(null)
    }
  }

  async function handleReject(requestId: number) {
    const reason = window.prompt('거절 사유를 입력하세요 (선택 사항)') ?? undefined
    setProcessingId(requestId)
    try {
      await rejectSignup(requestId, reason || undefined)
      setRequests((prev) => prev.filter((r) => r.requestId !== requestId))
    } catch {
      alert('거절 처리 중 오류가 발생했습니다.')
    } finally {
      setProcessingId(null)
    }
  }

  if (loading) return <p>불러오는 중...</p>
  if (error) return <p style={{ color: 'red' }}>{error}</p>

  return (
    <div>
      <h2>가입 신청 관리</h2>
      {requests.length === 0 ? (
        <p>대기 중인 가입 신청이 없습니다.</p>
      ) : (
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
              <tr key={req.requestId} style={{ borderBottom: '1px solid #eee' }}>
                <td style={{ padding: '8px' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    {req.picture && (
                      <img
                        src={req.picture}
                        alt={req.name}
                        style={{ width: 28, height: 28, borderRadius: '50%' }}
                      />
                    )}
                    {req.name}
                  </span>
                </td>
                <td style={{ padding: '8px' }}>{req.email}</td>
                <td style={{ padding: '8px' }}>
                  {new Date(req.requestedAt).toLocaleString('ko-KR')}
                </td>
                <td style={{ padding: '8px', display: 'flex', gap: '8px' }}>
                  <button
                    onClick={() => handleApprove(req.requestId)}
                    disabled={processingId === req.requestId}
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
                    onClick={() => handleReject(req.requestId)}
                    disabled={processingId === req.requestId}
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
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
