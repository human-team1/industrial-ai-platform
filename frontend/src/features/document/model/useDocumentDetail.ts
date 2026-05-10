import { useCallback, useEffect, useState } from 'react'
import { getDocumentDetail } from '../api'
import type { DocumentDetailResponse } from '../api/types'

export function useDocumentDetail(documentId: number) {
  const [detail, setDetail] = useState<DocumentDetailResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      setDetail(await getDocumentDetail(documentId))
    } catch (err) {
      setError(err instanceof Error ? err.message : '문서 상세 조회에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }, [documentId])

  useEffect(() => {
    if (Number.isFinite(documentId) && documentId > 0) {
      void load()
    }
  }, [documentId, load])

  return { detail, loading, error, reload: load }
}
