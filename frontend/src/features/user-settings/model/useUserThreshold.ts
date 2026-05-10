import { useEffect, useState } from 'react'
import {
  DEFAULT_USER_THRESHOLD,
  type UserThreshold,
} from '../../../entities/user-settings'
import { getMyThreshold } from '../api'
// saveMyThreshold는 thresholdId 의존을 가지므로 hook 내부 전용. 외부 노출 금지.
import { saveMyThreshold } from '../api/userThresholdApi'
import type { SaveMyThresholdRequest } from '../api/types'

export type UseUserThresholdResult = {
  threshold: UserThreshold
  hasServerValue: boolean
  loaded: boolean
  loadError: string | null
  setLocalAnomaly: (value: number) => void
  setLocalLowConfidence: (value: number) => void
  saveToServer: (payload: SaveMyThresholdRequest) => Promise<UserThreshold>
  reloadFromServer: () => void
}

export function useUserThreshold(): UseUserThresholdResult {
  const [threshold, setThreshold] = useState<UserThreshold>(DEFAULT_USER_THRESHOLD)
  const [hasServerValue, setHasServerValue] = useState(false)
  const [loaded, setLoaded] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    let cancelled = false

    setLoadError(null)
    getMyThreshold(controller.signal)
      .then((data) => {
        if (cancelled) return
        if (data) {
          setThreshold(data)
          setHasServerValue(true)
        } else {
          // GET 결과 없음 → 화면 초기값으로만 사용. 서버 저장 전까지 실제 서버 값으로 취급하지 않는다.
          setThreshold(DEFAULT_USER_THRESHOLD)
          setHasServerValue(false)
        }
        setLoaded(true)
      })
      .catch((error: unknown) => {
        if (cancelled || controller.signal.aborted) return
        setLoadError(error instanceof Error ? error.message : '임계값을 불러오지 못했습니다.')
        setLoaded(true)
      })

    return () => {
      cancelled = true
      controller.abort()
    }
  }, [reloadKey])

  function setLocalAnomaly(value: number) {
    setThreshold((prev) => ({ ...prev, anomalyThreshold: value }))
  }

  function setLocalLowConfidence(value: number) {
    setThreshold((prev) => ({ ...prev, lowConfidenceThreshold: value }))
  }

  async function saveToServer(payload: SaveMyThresholdRequest): Promise<UserThreshold> {
    const result = await saveMyThreshold(payload, threshold.thresholdId)
    setThreshold(result)
    setHasServerValue(true)
    return result
  }

  function reloadFromServer() {
    setReloadKey((k) => k + 1)
  }

  return {
    threshold,
    hasServerValue,
    loaded,
    loadError,
    setLocalAnomaly,
    setLocalLowConfidence,
    saveToServer,
    reloadFromServer,
  }
}
