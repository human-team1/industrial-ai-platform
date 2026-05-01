// ─── 서버 영속(USER_SETTING) ──────────────────────────────────────────────
export type UserSettings = {
  notificationEnabled: boolean
  defaultDashboardRange: string
  defaultCameraId: number | null
}

// ─── 서버 영속(USER_THRESHOLD) ────────────────────────────────────────────
export type ThresholdApplyScope = 'DEFAULT' | string

export type UserThreshold = {
  thresholdId: number | null
  anomalyThreshold: number
  lowConfidenceThreshold: number
  minAllowed: number
  maxAllowed: number
  applyScope: ThresholdApplyScope
  isActive: boolean
  updatedAt?: string
  thresholdVersion?: number
}

// ─── 클라이언트 전용(localStorage) ────────────────────────────────────────
export type UserPreferences = {
  entryPage: string
  refreshCycle: string
  theme: string
  autoLogout: string
  sessionKeep: boolean
  systemAlert: boolean
  reportAlert: boolean
  emailAlert: boolean
  sensitivity: string
  interval: string
}
