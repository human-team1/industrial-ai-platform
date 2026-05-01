// UI 옵션/뷰모델 타입 전용. 서버 DTO는 features/user-settings/api/types.ts 에 둔다.

import { THRESHOLD_PRESET_VALUE, type ThresholdPreset } from '../../../entities/user-settings'

// ─── 섹션 컴포넌트용 View-Model ───────────────────────────────────────────
export type DashboardView = {
  entryPage: string
  refreshCycle: string
  chartRange: string
  theme: string
}

export type NotificationView = {
  anomalyAlert: boolean
  systemAlert: boolean
  reportAlert: boolean
  emailAlert: boolean
}

export type DetectionView = {
  threshold: number
  sensitivity: string
  interval: string
}

export type SecurityView = {
  autoLogout: string
  sessionKeep: boolean
}

export type SummaryView = {
  entryPage: string
  refreshCycle: string
  chartRange: string
  theme: string
  anomalyAlert: boolean
  emailAlert: boolean
  threshold: number
  autoLogout: string
}

export type SelectOption<V extends string = string> = {
  value: V
  label: string
}

// ─── 임계값 빠른 선택 preset 버튼 옵션 ────────────────────────────────────
// dropdown은 제거되고, 사용자는 number input으로 직접 입력한다.
// preset 버튼은 자주 쓰는 값을 한 번에 채워주는 보조 UI.
export type ThresholdPresetOption = {
  value: ThresholdPreset
  label: string
  numericValue: number
}

export const THRESHOLD_PRESET_OPTIONS: ThresholdPresetOption[] = [
  { value: 'LOW', label: '낮음 (민감)', numericValue: THRESHOLD_PRESET_VALUE.LOW },
  { value: 'MEDIUM', label: '보통', numericValue: THRESHOLD_PRESET_VALUE.MEDIUM },
  { value: 'HIGH', label: '높음 (엄격)', numericValue: THRESHOLD_PRESET_VALUE.HIGH },
]

export const ENTRY_PAGE_OPTIONS: SelectOption[] = [
  { value: '대시보드', label: '대시보드' },
  { value: '실시간 탐지', label: '실시간 탐지' },
  { value: '탐지 이력', label: '탐지 이력' },
]

export const REFRESH_CYCLE_OPTIONS: SelectOption[] = [
  { value: '10초', label: '10초' },
  { value: '30초', label: '30초' },
  { value: '1분', label: '1분' },
  { value: '5분', label: '5분' },
  { value: '해제', label: '해제' },
]

export const CHART_RANGE_OPTIONS: SelectOption[] = [
  { value: '최근1일', label: '최근 1일' },
  { value: '최근7일', label: '최근 7일' },
  { value: '최근30일', label: '최근 30일' },
  { value: '최근90일', label: '최근 90일' },
]

export const THEME_OPTIONS: SelectOption[] = [
  { value: '라이트 모드', label: '라이트 모드' },
  { value: '다크 모드', label: '다크 모드' },
  { value: '시스템 설정', label: '시스템 설정' },
]

export const AUTO_LOGOUT_OPTIONS: SelectOption[] = [
  { value: '10분', label: '10분' },
  { value: '30분', label: '30분' },
  { value: '1시간', label: '1시간' },
  { value: '해제', label: '해제' },
]

export const SENSITIVITY_OPTIONS: SelectOption[] = [1, 2, 3, 4, 5].map((n) => ({
  value: String(n),
  label: `${n}단계`,
}))

export const INTERVAL_OPTIONS: SelectOption[] = [
  { value: '1초', label: '1초' },
  { value: '5초', label: '5초' },
  { value: '10초', label: '10초' },
  { value: '30초', label: '30초' },
]
