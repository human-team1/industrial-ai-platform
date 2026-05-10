import { useEffect, useState } from 'react'
import { fetchResultFilePreview } from '../api'

type State = {
  previewUrl: string | null
  isLoading: boolean
  error: string | null
}

export function useResultFilePreview(fileId?: number | null): State {
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!fileId) {
      setPreviewUrl(null)
      setIsLoading(false)
      setError(null)
      return
    }

    let objectUrl: string | null = null
    let cancelled = false
    setIsLoading(true)
    setError(null)

    fetchResultFilePreview(fileId)
      .then((blob) => {
        if (cancelled) return
        objectUrl = URL.createObjectURL(blob)
        setPreviewUrl(objectUrl)
      })
      .catch((err) => {
        if (cancelled) return
        setError(err instanceof Error ? err.message : '이미지를 불러오지 못했습니다.')
        setPreviewUrl(null)
      })
      .finally(() => {
        if (cancelled) return
        setIsLoading(false)
      })

    return () => {
      cancelled = true
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [fileId])

  return { previewUrl, isLoading, error }
}
