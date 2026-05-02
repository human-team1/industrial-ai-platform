import {
  useUserSettingsForm,
  DashboardDefaultsSection,
  NotificationPreferencesSection,
  RealTimeDetectionSection,
  SecurityPreferencesSection,
  SettingsSummary,
  SettingsActions,
  SettingsFeedback,
} from '../../features/user-settings'

export function UserSettingsWidget() {
  const {
    loading,
    saving,
    loadError,
    saveStatus,
    validationError,
    thresholdMin,
    thresholdMax,
    effectiveThresholdMin,
    lowConfidenceThreshold,
    hasValidThresholdInputRange,
    thresholdInputError,
    dashboard,
    notifications,
    detection,
    security,
    summary,
    updateDashboard,
    updateNotifications,
    updateDetection,
    updateSecurity,
    handleSave,
    handleCancel,
    handleReset,
  } = useUserSettingsForm()

  return (
    <div className="flex flex-col min-h-full">
      <div className="mb-6">
        <h1 className="text-[#374151] text-[22px] font-medium leading-none mb-1">설정</h1>
        <p className="text-[#374151] text-xs">개인 환경과 알림, 화면 기본값을 관리합니다.</p>
      </div>

      <SettingsFeedback
        loading={loading}
        loadError={loadError}
        saveStatus={saveStatus}
        validationError={validationError}
      />

      <div className="flex-1 grid grid-cols-2 gap-4 mb-4">
        <div className="flex flex-col gap-4">
          <NotificationPreferencesSection settings={notifications} onChange={updateNotifications} />
          <SecurityPreferencesSection settings={security} onChange={updateSecurity} />
        </div>

        <div className="flex flex-col gap-4">
          <DashboardDefaultsSection settings={dashboard} onChange={updateDashboard} />
          <RealTimeDetectionSection
            settings={detection}
            onChange={updateDetection}
            thresholdMin={thresholdMin}
            thresholdMax={thresholdMax}
            effectiveThresholdMin={effectiveThresholdMin}
            lowConfidenceThreshold={lowConfidenceThreshold}
            hasValidThresholdInputRange={hasValidThresholdInputRange}
            thresholdInputError={thresholdInputError}
          />
        </div>
      </div>

      <div className="mb-4">
        <SettingsSummary settings={summary} />
      </div>

      <SettingsActions
        onSave={handleSave}
        onCancel={handleCancel}
        onReset={handleReset}
        saving={saving}
      />
    </div>
  )
}
