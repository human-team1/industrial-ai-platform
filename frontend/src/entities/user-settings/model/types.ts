export type DashboardSettings = {
  entryPage: string
  refreshCycle: string
  chartRange: string
  theme: string
}

export type NotificationSettings = {
  anomalyAlert: boolean
  systemAlert: boolean
  reportAlert: boolean
  emailAlert: boolean
}

export type DetectionSettings = {
  threshold: string
  sensitivity: string
  interval: string
}

export type SecuritySettings = {
  autoLogout: string
  sessionKeep: boolean
}

export type AllSettings = {
  dashboard: DashboardSettings
  notifications: NotificationSettings
  detection: DetectionSettings
  security: SecuritySettings
}
