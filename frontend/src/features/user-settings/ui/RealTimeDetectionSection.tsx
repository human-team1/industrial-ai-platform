import { SettingsSectionCard, SettingsSelectField } from './primitives'
import type { DetectionSettings } from '../../../entities/user-settings'

export function RealTimeDetectionSection({
  settings,
  onChange,
}: {
  settings: DetectionSettings
  onChange: <K extends keyof DetectionSettings>(key: K, value: DetectionSettings[K]) => void
}) {
  return (
    <SettingsSectionCard title="실시간 탐지 설정" description="탐지 알고리즘의 동작 방식을 설정합니다.">
      <div className="flex flex-col gap-3">
        <SettingsSelectField
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
        <SettingsSelectField
          id="detection-sensitivity"
          label="민감도"
          value={settings.sensitivity}
          options={[1, 2, 3, 4, 5].map((n) => ({ value: String(n), label: `${n}단계` }))}
          onChange={(v) => onChange('sensitivity', v)}
        />
        <SettingsSelectField
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
    </SettingsSectionCard>
  )
}
