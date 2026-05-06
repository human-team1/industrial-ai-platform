import { useCallback, useState } from 'react'
import { getDocumentFilePreview } from '../api'

export function useDocumentFilePreview() {
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const open = useCallback(async (fileId?: number | null) => {
    if (!fileId) {
      setErrorMessage('미리보기 데이터를 제공하는 API가 없습니다.')
      return
    }

    try {
      setLoading(true)
      setErrorMessage(null)
      const blob = await getDocumentFilePreview(fileId)
      const url = URL.createObjectURL(blob)
      window.open(url, '_blank', 'noopener,noreferrer')
      window.setTimeout(() => URL.revokeObjectURL(url), 60_000)
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '미리보기를 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [])

  return { loading, errorMessage, open }
}
