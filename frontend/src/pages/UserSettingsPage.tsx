import {
  useUserSettings,
  DashboardDefaultsSection,
  NotificationPreferencesSection,
  RealTimeDetectionSection,
  SecurityPreferencesSection,
  SettingsSummary,
  SettingsActions,
} from '../features/user-settings'

export function UserSettingsPage() {
  const {
    settings,
    saved,
    updateDashboard,
    updateNotifications,
    updateDetection,
    updateSecurity,
    handleSave,
    handleCancel,
    handleReset,
  } = useUserSettings()

  return (
    <div className="flex flex-col min-h-full">
      <div className="mb-6">
        <h1 className="text-[#374151] text-[22px] font-medium leading-none mb-1">설정</h1>
        <p className="text-[#374151] text-xs">개인 환경과 알림, 화면 기본값을 관리합니다.</p>
      </div>

      {saved && (
        <div className="mb-4 flex items-center gap-2 px-4 py-2.5 bg-[#edf6ed] border border-[#b7ddb7] rounded text-[#3d7a3d] text-sm w-fit">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 16 16" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M3 8l4 4 6-6" />
          </svg>
          설정이 저장되었습니다.
        </div>
      )}

      <div className="flex-1 grid grid-cols-2 gap-4 mb-4">
        <div className="flex flex-col gap-4">
          <NotificationPreferencesSection settings={settings.notifications} onChange={updateNotifications} />
          <SecurityPreferencesSection settings={settings.security} onChange={updateSecurity} />
        </div>

        <div className="flex flex-col gap-4">
          <DashboardDefaultsSection settings={settings.dashboard} onChange={updateDashboard} />
          <RealTimeDetectionSection settings={settings.detection} onChange={updateDetection} />
        </div>
      </div>

      <div className="mb-4">
        <SettingsSummary settings={settings} />
      </div>

      <SettingsActions onSave={handleSave} onCancel={handleCancel} onReset={handleReset} />
    </div>
  )
}
