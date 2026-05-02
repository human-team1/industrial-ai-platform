import { useEffect, useState } from 'react'
import { approveSignup, getSignupRequests, rejectSignup } from '../api'
import type { SignupRequestSummary } from '../types'

type UseAdminSignupRequestsResult = {
  requests: SignupRequestSummary[]
  loading: boolean
  error: string | null
  processingId: number | null
  approve: (requestId: number) => Promise<void>
  reject: (requestId: number) => Promise<void>
  reload: () => Promise<void>
}

export function useAdminSignupRequests(): UseAdminSignupRequestsResult {
  const [requests, setRequests] = useState<SignupRequestSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [processingId, setProcessingId] = useState<number | null>(null)

  async function load() {
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

  useEffect(() => {
    load()
  }, [])

  async function approve(requestId: number) {
    if (processingId !== null) return
    setProcessingId(requestId)
    try {
      await approveSignup(requestId)
      setRequests((prev) => prev.filter((r) => r.requestId !== requestId))
    } catch {
      window.alert('승인 처리 중 오류가 발생했습니다.')
    } finally {
      setProcessingId(null)
    }
  }

  async function reject(requestId: number) {
    if (processingId !== null) return
    const input = window.prompt('거절 사유를 입력하세요 (선택 사항)')
    if (input === null) return
    const trimmed = input.trim()
    const rejectReason = trimmed.length > 0 ? trimmed : undefined

    setProcessingId(requestId)
    try {
      await rejectSignup(requestId, rejectReason)
      setRequests((prev) => prev.filter((r) => r.requestId !== requestId))
    } catch {
      window.alert('거절 처리 중 오류가 발생했습니다.')
    } finally {
      setProcessingId(null)
    }
  }

  return {
    requests,
    loading,
    error,
    processingId,
    approve,
    reject,
    reload: load,
  }
}
