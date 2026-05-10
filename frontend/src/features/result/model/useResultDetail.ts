import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchResultDetail } from '../api'
import type { ResultDetail } from '../api/types'

export function useResultDetail(resultIdParam: string | undefined) {
  const navigate = useNavigate()
  const [data, setData] = useState<ResultDetail | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const resultId = Number(resultIdParam)
  const invalidResultId = !resultIdParam || Number.isNaN(resultId) || resultId < 1

  const load = useCallback(async () => {
    if (invalidResultId) {
      setData(null)
      setError('유효하지 않은 검사 결과 ID입니다.')
      return
    }

    try {
      setLoading(true)
      setError(null)
      const result = await fetchResultDetail(resultId)
      setData(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : '검사 결과 상세를 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [invalidResultId, resultId])

  useEffect(() => {
    void load()
  }, [load])

  return {
    data,
    loading,
    error,
    retry: load,
    goBack: () => navigate('/results'),
  }
}
