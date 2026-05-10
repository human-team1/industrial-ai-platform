import { useEffect, useState } from 'react'
import {
  DEFAULT_USER_SETTINGS,
  type UserSettings,
} from '../../../entities/user-settings'
import { getMySettings, patchMySettings } from '../api'
import type { PatchMySettingsRequest } from '../api/types'

export type UseUserSettingsResult = {
  settings: UserSettings
  loaded: boolean
  loadError: string | null
  setLocal: <K extends keyof UserSettings>(key: K, value: UserSettings[K]) => void
  reloadFromServer: () => void
  applyServerResponse: (next: UserSettings) => void
  saveToServer: (payload: PatchMySettingsRequest) => Promise<UserSettings>
}

export function useUserSettings(): UseUserSettingsResult {
  const [settings, setSettings] = useState<UserSettings>(DEFAULT_USER_SETTINGS)
  const [loaded, setLoaded] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    let cancelled = false

    setLoadError(null)
    getMySettings(controller.signal)
      .then((data) => {
        if (cancelled) return
        setSettings(data)
        setLoaded(true)
      })
      .catch((error: unknown) => {
        if (cancelled || controller.signal.aborted) return
        setLoadError(error instanceof Error ? error.message : '설정을 불러오지 못했습니다.')
        setLoaded(true)
      })

    return () => {
      cancelled = true
      controller.abort()
    }
  }, [reloadKey])

  function setLocal<K extends keyof UserSettings>(key: K, value: UserSettings[K]) {
    setSettings((prev) => ({ ...prev, [key]: value }))
  }

  function reloadFromServer() {
    setReloadKey((k) => k + 1)
  }

  function applyServerResponse(next: UserSettings) {
    setSettings(next)
  }

  async function saveToServer(payload: PatchMySettingsRequest): Promise<UserSettings> {
    const result = await patchMySettings(payload)
    setSettings(result)
    return result
  }

  return {
    settings,
    loaded,
    loadError,
    setLocal,
    reloadFromServer,
    applyServerResponse,
    saveToServer,
  }
}
