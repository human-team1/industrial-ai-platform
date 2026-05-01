import {
  useUserSettingsForm,
  DashboardDefaultsSection,
  NotificationPreferencesSection,
  RealTimeDetectionSection,
  SecurityPreferencesSection,
  SettingsSummary,
  SettingsActions,
} from '../features/user-settings'

export function UserSettingsPage() {
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

      {loading && (
        <div className="mb-4 flex items-center gap-2 px-4 py-2.5 bg-[#eef3fb] border border-[#bcd0ec] rounded text-[#1f4f8a] text-sm w-fit">
          설정을 불러오는 중...
        </div>
      )}

      {loadError && (
        <div className="mb-4 flex items-start gap-2 px-4 py-2.5 bg-[#fcebea] border border-[#f3b9b6] rounded text-[#a13b34] text-sm w-fit">
          {loadError}
        </div>
      )}

      {saveStatus.kind === 'success' && (
        <div className="mb-4 flex items-center gap-2 px-4 py-2.5 bg-[#edf6ed] border border-[#b7ddb7] rounded text-[#3d7a3d] text-sm w-fit">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 16 16" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M3 8l4 4 6-6" />
          </svg>
          설정이 저장되었습니다.
        </div>
      )}

      {saveStatus.kind === 'noop' && (
        <div className="mb-4 flex items-center gap-2 px-4 py-2.5 bg-[#f4f5f7] border border-[#d8dbe1] rounded text-[#4b5563] text-sm w-fit">
          변경된 설정이 없습니다.
        </div>
      )}

      {saveStatus.kind === 'partial' && (
        <div className="mb-4 px-4 py-2.5 bg-[#fff8e1] border border-[#f3d27a] rounded text-[#8a6d1c] text-sm">
          <div className="font-medium">일부 설정만 저장되었습니다.</div>
          <ul className="list-disc pl-5 mt-1 text-xs">
            {saveStatus.messages.map((m) => (
              <li key={m}>{m}</li>
            ))}
          </ul>
        </div>
      )}

      {saveStatus.kind === 'error' && (
        <div className="mb-4 px-4 py-2.5 bg-[#fcebea] border border-[#f3b9b6] rounded text-[#a13b34] text-sm">
          <div className="font-medium">설정 저장에 실패했습니다.</div>
          <ul className="list-disc pl-5 mt-1 text-xs">
            {saveStatus.messages.map((m) => (
              <li key={m}>{m}</li>
            ))}
          </ul>
        </div>
      )}

      {validationError && saveStatus.kind !== 'error' && (
        <div className="mb-4 px-4 py-2.5 bg-[#fcebea] border border-[#f3b9b6] rounded text-[#a13b34] text-sm w-fit">
          {validationError}
        </div>
      )}

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
