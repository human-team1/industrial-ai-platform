import type { UserPreferences, UserSettings, UserThreshold } from './types'

// USER_SETTING 미존재 시 화면 초기값. 서버 응답 값이 있으면 절대 덮어쓰지 않는다.
export const DEFAULT_USER_SETTINGS: UserSettings = {
  notificationEnabled: true,
  defaultDashboardRange: '최근7일',
  defaultCameraId: null,
}

// USER_THRESHOLD 미존재 시 화면 초기값. 서버 저장 전까지는 실제 서버 값으로 취급하지 않는다.
// lowConfidenceThreshold 기본값 0.55는 GET 결과가 비어 있을 때만 사용한다.
export const DEFAULT_USER_THRESHOLD: UserThreshold = {
  thresholdId: null,
  anomalyThreshold: 0.75,
  lowConfidenceThreshold: 0.55,
  minAllowed: 0.0,
  maxAllowed: 1.0,
  applyScope: 'DEFAULT',
  isActive: true,
}

// localStorage preference 기본값
export const DEFAULT_USER_PREFERENCES: UserPreferences = {
  entryPage: '대시보드',
  refreshCycle: '30초',
  theme: '라이트 모드',
  autoLogout: '30분',
  sessionKeep: true,
  systemAlert: true,
  reportAlert: false,
  emailAlert: false,
  sensitivity: '3',
  interval: '5초',
}
