import { useEffect, useState } from 'react'
import type { OperationPolicy } from '../../../entities/operation/model/types'
import { fetchOperationPolicies, updateOperationPolicy } from '../api'

export function useOperationPolicies() {
  const [category, setCategory] = useState('')
  const [activeOnly, setActiveOnly] = useState(false)
  const [policies, setPolicies] = useState<OperationPolicy[]>([])
  const [loading, setLoading] = useState(false)
  const [savingId, setSavingId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    setLoading(true)
    setError(null)
    fetchOperationPolicies({ category, activeOnly }, controller.signal)
      .then(setPolicies)
      .catch((err) => {
        if (!controller.signal.aborted) {
          setError(err instanceof Error ? err.message : '운영 정책을 불러오지 못했습니다.')
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [category, activeOnly, reloadKey])

  async function savePolicy(policy: OperationPolicy, policyValue: string, isActive: boolean): Promise<boolean> {
    setSavingId(policy.operationPolicyId)
    setError(null)
    setMessage(null)
    try {
      const updated = await updateOperationPolicy(policy.operationPolicyId, { policyValue, isActive })
      setPolicies((items) => items.map((item) => item.operationPolicyId === updated.operationPolicyId ? updated : item))
      setMessage('운영 정책을 저장했습니다.')
      return true
    } catch (err) {
      setError(err instanceof Error ? err.message : '운영 정책 저장에 실패했습니다.')
      return false
    } finally {
      setSavingId(null)
    }
  }

  return {
    category,
    setCategory,
    activeOnly,
    setActiveOnly,
    policies,
    loading,
    savingId,
    error,
    message,
    reload: () => setReloadKey((value) => value + 1),
    savePolicy,
  }
}
