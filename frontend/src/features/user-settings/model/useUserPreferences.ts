import { useEffect, useState } from 'react'
import {
  DEFAULT_USER_PREFERENCES,
  type UserPreferences,
} from '../../../entities/user-settings'

const STORAGE_KEY = 'userSettingsPreferences'

function loadFromStorage(): UserPreferences {
  if (typeof window === 'undefined') return { ...DEFAULT_USER_PREFERENCES }
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) return { ...DEFAULT_USER_PREFERENCES }
    const parsed = JSON.parse(raw) as Partial<UserPreferences>
    // 일부 키만 저장돼 있어도 누락 키는 기본값으로 채운다.
    return { ...DEFAULT_USER_PREFERENCES, ...parsed }
  } catch {
    return { ...DEFAULT_USER_PREFERENCES }
  }
}

export type UseUserPreferencesResult = {
  preferences: UserPreferences
  setLocal: <K extends keyof UserPreferences>(key: K, value: UserPreferences[K]) => void
  persist: () => void
}

// 클라이언트 전용 설정. 서버 ERD에 컬럼이 없으므로 서버 payload에 포함하지 않는다.
// 변경 즉시 localStorage에 반영해도 되지만, 페이지 저장 흐름과 정합을 맞추기 위해 persist()를 따로 둔다.
export function useUserPreferences(): UseUserPreferencesResult {
  const [preferences, setPreferences] = useState<UserPreferences>(() => loadFromStorage())

  useEffect(() => {
    if (typeof window === 'undefined') return
    function handleStorage(e: StorageEvent) {
      if (e.key !== STORAGE_KEY) return
      setPreferences(loadFromStorage())
    }
    window.addEventListener('storage', handleStorage)
    return () => window.removeEventListener('storage', handleStorage)
  }, [])

  function setLocal<K extends keyof UserPreferences>(key: K, value: UserPreferences[K]) {
    setPreferences((prev) => ({ ...prev, [key]: value }))
  }

  function persist() {
    if (typeof window === 'undefined') return
    try {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences))
    } catch {
      // quota 초과 등은 무시 — 다음 저장 시 재시도된다.
    }
  }

  return { preferences, setLocal, persist }
}
