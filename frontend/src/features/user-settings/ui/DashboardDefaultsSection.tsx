import { SettingsSectionCard, SettingsSelectField } from './primitives'
import type { DashboardSettings } from '../../../entities/user-settings'

export function DashboardDefaultsSection({
  settings,
  onChange,
}: {
  settings: DashboardSettings
  onChange: <K extends keyof DashboardSettings>(key: K, value: DashboardSettings[K]) => void
}) {
  return (
    <SettingsSectionCard title="대시보드 기본 설정" description="대시보드의 기본 표시 방식을 설정합니다.">
      <div className="flex flex-col gap-3">
        <SettingsSelectField
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
        <SettingsSelectField
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
        <SettingsSelectField
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
        <SettingsSelectField
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
    </SettingsSectionCard>
  )
}
