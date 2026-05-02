import { SettingsSectionCard, SettingsSelectField } from './primitives'
import {
  CHART_RANGE_OPTIONS,
  ENTRY_PAGE_OPTIONS,
  REFRESH_CYCLE_OPTIONS,
  THEME_OPTIONS,
  type DashboardView,
} from '../types'

export function DashboardDefaultsSection({
  settings,
  onChange,
}: {
  settings: DashboardView
  onChange: <K extends keyof DashboardView>(key: K, value: DashboardView[K]) => void
}) {
  return (
    <SettingsSectionCard title="대시보드 기본 설정" description="대시보드의 기본 표시 방식을 설정합니다.">
      <div className="flex flex-col gap-3">
        <SettingsSelectField
          id="default-entry-page"
          label="기본 진입 페이지"
          value={settings.entryPage}
          options={ENTRY_PAGE_OPTIONS}
          onChange={(v) => onChange('entryPage', v)}
        />
        <SettingsSelectField
          id="auto-refresh-cycle"
          label="자동 새로고침 주기"
          value={settings.refreshCycle}
          options={REFRESH_CYCLE_OPTIONS}
          onChange={(v) => onChange('refreshCycle', v)}
        />
        <SettingsSelectField
          id="default-chart-range"
          label="기본 차트 기간"
          value={settings.chartRange}
          options={CHART_RANGE_OPTIONS}
          onChange={(v) => onChange('chartRange', v)}
        />
        <SettingsSelectField
          id="theme"
          label="테마"
          value={settings.theme}
          options={THEME_OPTIONS}
          onChange={(v) => onChange('theme', v)}
        />
      </div>
    </SettingsSectionCard>
  )
}
