// 서버 DTO. UI 옵션 타입과 분리. (UI 옵션은 features/user-settings/types/index.ts)

// ─── /users/me/settings ───────────────────────────────────────────────────
export type GetMySettingsResponse = {
  notificationEnabled?: boolean
  defaultDashboardRange?: string | null
  defaultCameraId?: number | null
}

export type PatchMySettingsRequest = Partial<{
  notificationEnabled: boolean
  defaultDashboardRange: string
  defaultCameraId: number | null
}>

// ─── /users/me/thresholds ─────────────────────────────────────────────────
export type GetMyThresholdResponse = {
  thresholdId?: number
  anomalyThreshold?: number
  lowConfidenceThreshold?: number
  minAllowed?: number
  maxAllowed?: number
  applyScope?: string
  isActive?: boolean
  updatedAt?: string
  thresholdVersion?: number
} | null

// 생성/수정 추상화 입력. 훅/폼은 thresholdId에 직접 의존하지 않는다.
export type SaveMyThresholdRequest = {
  anomalyThreshold: number
  lowConfidenceThreshold: number
  applyScope?: string
  changeReason?: string
}

export type PatchMyThresholdRequest = SaveMyThresholdRequest

// USER_THRESHOLD_HISTORY 변경 이력 정책상 필요한 기본 사유.
// UI에서 사유를 입력받지 않으므로 일관된 식별 키를 사용한다.
export const DEFAULT_THRESHOLD_CHANGE_REASON = 'USER_SETTING_PAGE_UPDATE'
