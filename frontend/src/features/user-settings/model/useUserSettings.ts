import { useState } from 'react'
import {
  DEFAULT_SETTINGS,
  type AllSettings,
  type DashboardSettings,
  type DetectionSettings,
  type NotificationSettings,
  type SecuritySettings,
} from '../../../entities/user-settings'

export function useUserSettings() {
  const [settings, setSettings] = useState<AllSettings>(DEFAULT_SETTINGS)
  const [saved, setSaved] = useState(false)

  function updateDashboard<K extends keyof DashboardSettings>(key: K, value: DashboardSettings[K]) {
    setSettings((prev) => ({ ...prev, dashboard: { ...prev.dashboard, [key]: value } }))
    setSaved(false)
  }

  function updateNotifications<K extends keyof NotificationSettings>(key: K, value: NotificationSettings[K]) {
    setSettings((prev) => ({ ...prev, notifications: { ...prev.notifications, [key]: value } }))
    setSaved(false)
  }

  function updateDetection<K extends keyof DetectionSettings>(key: K, value: DetectionSettings[K]) {
    setSettings((prev) => ({ ...prev, detection: { ...prev.detection, [key]: value } }))
    setSaved(false)
  }

  function updateSecurity<K extends keyof SecuritySettings>(key: K, value: SecuritySettings[K]) {
    setSettings((prev) => ({ ...prev, security: { ...prev.security, [key]: value } }))
    setSaved(false)
  }

  function handleSave() {
    // TODO: API 연동 (백엔드 계약 확정 후)
    setSaved(true)
  }

  function handleCancel() {
    setSettings(DEFAULT_SETTINGS)
    setSaved(false)
  }

  function handleReset() {
    setSettings(DEFAULT_SETTINGS)
    setSaved(false)
  }

  return {
    settings,
    saved,
    updateDashboard,
    updateNotifications,
    updateDetection,
    updateSecurity,
    handleSave,
    handleCancel,
    handleReset,
  }
}
