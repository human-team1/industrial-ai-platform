import type { AllSettings } from './types'

export const DEFAULT_SETTINGS: AllSettings = {
  dashboard: {
    entryPage: '대시보드',
    refreshCycle: '30초',
    chartRange: '최근7일',
    theme: '라이트 모드',
  },
  notifications: {
    anomalyAlert: true,
    systemAlert: true,
    reportAlert: false,
    emailAlert: false,
  },
  detection: {
    threshold: '보통',
    sensitivity: '3',
    interval: '5초',
  },
  security: {
    autoLogout: '30분',
    sessionKeep: true,
  },
}
