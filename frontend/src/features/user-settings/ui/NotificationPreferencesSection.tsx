import { SettingsSectionCard, SettingsToggleSwitch } from './primitives'
import type { NotificationView } from '../types'

export function NotificationPreferencesSection({
  settings,
  onChange,
}: {
  settings: NotificationView
  onChange: <K extends keyof NotificationView>(key: K, value: NotificationView[K]) => void
}) {
  const items: { key: keyof NotificationView; label: string; desc: string }[] = [
    { key: 'anomalyAlert', label: '이상 탐지 알림', desc: '이상이 감지되면 즉시 알림을 받습니다.' },
    { key: 'systemAlert', label: '시스템 점검 알림', desc: '예정된 점검 및 시스템 상태 변경 알림입니다.' },
    { key: 'reportAlert', label: '보고서 생성 알림', desc: '보고서 생성이 완료되면 알림을 받습니다.' },
    { key: 'emailAlert', label: '이메일 알림', desc: '중요 알림을 이메일로도 수신합니다.' },
  ]

  return (
    <SettingsSectionCard title="알림 환경 설정" description="수신할 알림 유형을 선택합니다.">
      <div className="flex flex-col gap-3">
        {items.map((item) => (
          <div key={item.key} className="flex items-center justify-between gap-4">
            <div>
              <div className="text-[#374151] text-[13px] mb-0.5">{item.label}</div>
              <div className="text-[#6b7280] text-[11px]">{item.desc}</div>
            </div>
            <SettingsToggleSwitch
              id={`toggle-${item.key}`}
              checked={settings[item.key]}
              onChange={(v) => onChange(item.key, v)}
            />
          </div>
        ))}
      </div>
    </SettingsSectionCard>
  )
}
