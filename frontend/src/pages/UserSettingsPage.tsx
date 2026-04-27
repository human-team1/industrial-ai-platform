import { useState } from 'react'

// ─── Types ────────────────────────────────────────────────────────────────────

type DashboardSettings = {
  entryPage: string
  refreshCycle: string
  chartRange: string
  theme: string
}

type NotificationSettings = {
  anomalyAlert: boolean
  systemAlert: boolean
  reportAlert: boolean
  emailAlert: boolean
}

type DetectionSettings = {
  threshold: string
  sensitivity: string
  interval: string
}

type SecuritySettings = {
  autoLogout: string
  sessionKeep: boolean
}

type AllSettings = {
  dashboard: DashboardSettings
  notifications: NotificationSettings
  detection: DetectionSettings
  security: SecuritySettings
}

const DEFAULT_SETTINGS: AllSettings = {
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

// ─── Shared UI ────────────────────────────────────────────────────────────────

function SectionCard({
  title,
  description,
  children,
}: {
  title: string
  description: string
  children: React.ReactNode
}) {
  return (
    <div className="bg-[#fefefe] border border-[#f0f1f5] rounded p-5 flex flex-col gap-4">
      <div>
        <h2 className="text-[#4b5563] text-base font-normal leading-none mb-1">{title}</h2>
        <p className="text-[#374151] text-[11px]">{description}</p>
      </div>
      {children}
    </div>
  )
}

function SelectField({
  id,
  label,
  value,
  options,
  onChange,
}: {
  id: string
  label: string
  value: string
  options: { value: string; label: string }[]
  onChange: (v: string) => void
}) {
  return (
    <div>
      <label htmlFor={id} className="block text-[#374151] text-[11px] mb-1.5">
        {label}
      </label>
      <div className="relative">
        <select
          id={id}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className="w-full appearance-none px-3 py-2 bg-[#fefefe] border border-[#f0f1f5] rounded-[3px] text-[#4b5563] text-xs outline-none focus:border-[#4a90e2] transition-colors pr-7 cursor-pointer"
        >
          {options.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
        <svg
          className="absolute right-2.5 top-1/2 -translate-y-1/2 w-2.5 h-1.5 pointer-events-none text-[#c3c7d2]"
          fill="none"
          viewBox="0 0 10 6"
          stroke="currentColor"
          strokeWidth={1.5}
        >
          <path strokeLinecap="round" strokeLinejoin="round" d="M1 1l4 4 4-4" />
        </svg>
      </div>
    </div>
  )
}

function ToggleSwitch({
  id,
  checked,
  onChange,
}: {
  id: string
  checked: boolean
  onChange: (v: boolean) => void
}) {
  return (
    <button
      id={id}
      type="button"
      role="switch"
      aria-checked={checked}
      onClick={() => onChange(!checked)}
      className={`relative w-9 h-5 rounded-full transition-colors shrink-0 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0] ${
        checked ? 'bg-[#1166e0]' : 'bg-[#e2e4ea]'
      }`}
    >
      <span
        className={`absolute top-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform ${
          checked ? 'translate-x-[18px]' : 'translate-x-0.5'
        }`}
      />
    </button>
  )
}

// ─── Dashboard Defaults Section ───────────────────────────────────────────────

function DashboardDefaultsSection({
  settings,
  onChange,
}: {
  settings: DashboardSettings
  onChange: <K extends keyof DashboardSettings>(key: K, value: DashboardSettings[K]) => void
}) {
  return (
    <SectionCard
      title="대시보드 기본 설정"
      description="대시보드의 기본 표시 방식을 설정합니다."
    >
      <div className="flex flex-col gap-3">
        <SelectField
          id="default-entry-page"
          label="기본 진입 페이지"
          value={settings.entryPage}
          options={[
            { value: '대시보드', label: '대시보드' },
            { value: '실시간 탐지', label: '실시간 탐지' },
            { value: '탐지 이력', label: '탐지 이력' },
          ]}
          onChange={(v) => onChange('entryPage', v)}
        />
        <SelectField
          id="auto-refresh-cycle"
          label="자동 새로고침 주기"
          value={settings.refreshCycle}
          options={[
            { value: '10초', label: '10초' },
            { value: '30초', label: '30초' },
            { value: '1분', label: '1분' },
            { value: '5분', label: '5분' },
            { value: '해제', label: '해제' },
          ]}
          onChange={(v) => onChange('refreshCycle', v)}
        />
        <SelectField
          id="default-chart-range"
          label="기본 차트 기간"
          value={settings.chartRange}
          options={[
            { value: '최근1일', label: '최근 1일' },
            { value: '최근7일', label: '최근 7일' },
            { value: '최근30일', label: '최근 30일' },
            { value: '최근90일', label: '최근 90일' },
          ]}
          onChange={(v) => onChange('chartRange', v)}
        />
        <SelectField
          id="theme"
          label="테마"
          value={settings.theme}
          options={[
            { value: '라이트 모드', label: '라이트 모드' },
            { value: '다크 모드', label: '다크 모드' },
            { value: '시스템 설정', label: '시스템 설정' },
          ]}
          onChange={(v) => onChange('theme', v)}
        />
      </div>
    </SectionCard>
  )
}

// ─── Notification Preferences Section ────────────────────────────────────────

function NotificationPreferencesSection({
  settings,
  onChange,
}: {
  settings: NotificationSettings
  onChange: <K extends keyof NotificationSettings>(key: K, value: NotificationSettings[K]) => void
}) {
  const items: { key: keyof NotificationSettings; label: string; desc: string }[] = [
    { key: 'anomalyAlert', label: '이상 탐지 알림', desc: '이상이 감지되면 즉시 알림을 받습니다.' },
    { key: 'systemAlert', label: '시스템 점검 알림', desc: '예정된 점검 및 시스템 상태 변경 알림입니다.' },
    { key: 'reportAlert', label: '보고서 생성 알림', desc: '보고서 생성이 완료되면 알림을 받습니다.' },
    { key: 'emailAlert', label: '이메일 알림', desc: '중요 알림을 이메일로도 수신합니다.' },
  ]

  return (
    <SectionCard title="알림 환경 설정" description="수신할 알림 유형을 선택합니다.">
      <div className="flex flex-col gap-3">
        {items.map((item) => (
          <div key={item.key} className="flex items-center justify-between gap-4">
            <div>
              <div className="text-[#374151] text-[13px] mb-0.5">{item.label}</div>
              <div className="text-[#6b7280] text-[11px]">{item.desc}</div>
            </div>
            <ToggleSwitch
              id={`toggle-${item.key}`}
              checked={settings[item.key]}
              onChange={(v) => onChange(item.key, v)}
            />
          </div>
        ))}
      </div>
    </SectionCard>
  )
}

// ─── Real-Time Detection Section ──────────────────────────────────────────────

function RealTimeDetectionSection({
  settings,
  onChange,
}: {
  settings: DetectionSettings
  onChange: <K extends keyof DetectionSettings>(key: K, value: DetectionSettings[K]) => void
}) {
  return (
    <SectionCard title="실시간 탐지 설정" description="탐지 알고리즘의 동작 방식을 설정합니다.">
      <div className="flex flex-col gap-3">
        <SelectField
          id="detection-threshold"
          label="탐지 임계값"
          value={settings.threshold}
          options={[
            { value: '낮음', label: '낮음 (민감)' },
            { value: '보통', label: '보통' },
            { value: '높음', label: '높음 (엄격)' },
          ]}
          onChange={(v) => onChange('threshold', v)}
        />
        <SelectField
          id="detection-sensitivity"
          label="민감도"
          value={settings.sensitivity}
          options={[1, 2, 3, 4, 5].map((n) => ({ value: String(n), label: `${n}단계` }))}
          onChange={(v) => onChange('sensitivity', v)}
        />
        <SelectField
          id="detection-interval"
          label="탐지 주기"
          value={settings.interval}
          options={[
            { value: '1초', label: '1초' },
            { value: '5초', label: '5초' },
            { value: '10초', label: '10초' },
            { value: '30초', label: '30초' },
          ]}
          onChange={(v) => onChange('interval', v)}
        />
      </div>
    </SectionCard>
  )
}

// ─── Security Preferences Section ────────────────────────────────────────────

function SecurityPreferencesSection({
  settings,
  onChange,
}: {
  settings: SecuritySettings
  onChange: <K extends keyof SecuritySettings>(key: K, value: SecuritySettings[K]) => void
}) {
  return (
    <SectionCard title="보안 환경 설정" description="계정 보안과 세션 관련 설정을 관리합니다.">
      <div className="flex flex-col gap-3">
        <SelectField
          id="auto-logout"
          label="자동 로그아웃"
          value={settings.autoLogout}
          options={[
            { value: '10분', label: '10분' },
            { value: '30분', label: '30분' },
            { value: '1시간', label: '1시간' },
            { value: '해제', label: '해제' },
          ]}
          onChange={(v) => onChange('autoLogout', v)}
        />
        <div className="flex items-center justify-between gap-4">
          <div>
            <div className="text-[#374151] text-[13px] mb-0.5">세션 유지</div>
            <div className="text-[#6b7280] text-[11px]">브라우저를 닫아도 로그인 상태를 유지합니다.</div>
          </div>
          <ToggleSwitch
            id="toggle-session-keep"
            checked={settings.sessionKeep}
            onChange={(v) => onChange('sessionKeep', v)}
          />
        </div>
      </div>
    </SectionCard>
  )
}

// ─── Settings Summary Section ─────────────────────────────────────────────────

function SettingsSummarySection({ settings }: { settings: AllSettings }) {
  const summaryItems = [
    { label: '기본 진입 페이지', value: settings.dashboard.entryPage },
    { label: '새로고침 주기', value: settings.dashboard.refreshCycle },
    { label: '차트 기간', value: settings.dashboard.chartRange },
    { label: '테마', value: settings.dashboard.theme },
    { label: '이상 탐지 알림', value: settings.notifications.anomalyAlert ? '켜짐' : '꺼짐' },
    { label: '이메일 알림', value: settings.notifications.emailAlert ? '켜짐' : '꺼짐' },
    { label: '탐지 임계값', value: settings.detection.threshold },
    { label: '자동 로그아웃', value: settings.security.autoLogout },
  ]

  return (
    <SectionCard title="설정 요약" description="현재 적용 중인 설정의 요약입니다.">
      <div className="grid grid-cols-2 gap-x-6 gap-y-2">
        {summaryItems.map((item) => (
          <div key={item.label} className="flex items-center justify-between py-1.5 border-b border-[#f5f6fa]">
            <span className="text-[#6b7280] text-[11px]">{item.label}</span>
            <span className="text-[#374151] text-[12px] font-medium">{item.value}</span>
          </div>
        ))}
      </div>
    </SectionCard>
  )
}

// ─── Help and Actions Section ─────────────────────────────────────────────────

function HelpAndActionsSection({
  onSave,
  onCancel,
  onReset,
}: {
  onSave: () => void
  onCancel: () => void
  onReset: () => void
}) {
  return (
    <div className="bg-[#fdfdfd] border-t-[6px] border-[#fafafc] px-5 py-3 flex items-center justify-between">
      {/* 기본값 복원 */}
      <button
        type="button"
        onClick={onReset}
        className="flex items-center gap-2 px-4 py-2 bg-[#fdfdfd] border border-[#f3f4f7] rounded-[3px] text-[#4b5563] text-xs hover:bg-[#f5f6fa] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]"
      >
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 14 14" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M1 7a6 6 0 106-6H3M3 1v3h3" />
        </svg>
        기본값으로 복원
      </button>

      {/* 취소 / 저장 */}
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={onCancel}
          className="px-7 py-2 bg-[#fffefe] border border-[#e3e1e0] rounded-[5.75px] text-[#4b5563] text-xs hover:bg-[#f5f6fa] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]"
        >
          취소
        </button>
        <button
          type="submit"
          onClick={onSave}
          className="flex items-center gap-1.5 px-7 py-2 bg-[#0965e3] border border-[#0458e5] rounded-[5.25px] text-[#88b5ef] text-[13px] hover:bg-[#0755cc] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]"
        >
          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 14 14" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M2 7l4 4 6-6" />
          </svg>
          저장
        </button>
      </div>
    </div>
  )
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export function UserSettingsPage() {
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
    // TODO: API 연동
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

  return (
    <div className="flex flex-col min-h-full">
      {/* 헤더 */}
      <div className="mb-6">
        <h1 className="text-[#374151] text-[22px] font-medium leading-none mb-1">설정</h1>
        <p className="text-[#374151] text-xs">개인 환경과 알림, 화면 기본값을 관리합니다.</p>
      </div>

      {/* 저장 완료 토스트 */}
      {saved && (
        <div className="mb-4 flex items-center gap-2 px-4 py-2.5 bg-[#edf6ed] border border-[#b7ddb7] rounded text-[#3d7a3d] text-sm w-fit">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 16 16" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M3 8l4 4 6-6" />
          </svg>
          설정이 저장되었습니다.
        </div>
      )}

      {/* 메인 그리드 */}
      <div className="flex-1 grid grid-cols-2 gap-4 mb-4">
        {/* 좌측 컬럼 */}
        <div className="flex flex-col gap-4">
          <NotificationPreferencesSection settings={settings.notifications} onChange={updateNotifications} />
          <SecurityPreferencesSection settings={settings.security} onChange={updateSecurity} />
        </div>

        {/* 우측 컬럼 */}
        <div className="flex flex-col gap-4">
          <DashboardDefaultsSection settings={settings.dashboard} onChange={updateDashboard} />
          <RealTimeDetectionSection settings={settings.detection} onChange={updateDetection} />
        </div>
      </div>

      {/* 설정 요약 */}
      <div className="mb-4">
        <SettingsSummarySection settings={settings} />
      </div>

      {/* 액션 버튼 */}
      <HelpAndActionsSection
        onSave={handleSave}
        onCancel={handleCancel}
        onReset={handleReset}
      />
    </div>
  )
}