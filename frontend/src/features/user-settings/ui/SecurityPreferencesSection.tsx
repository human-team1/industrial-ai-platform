import { SettingsSectionCard, SettingsSelectField, SettingsToggleSwitch } from './primitives'
import type { SecuritySettings } from '../../../entities/user-settings'

export function SecurityPreferencesSection({
  settings,
  onChange,
}: {
  settings: SecuritySettings
  onChange: <K extends keyof SecuritySettings>(key: K, value: SecuritySettings[K]) => void
}) {
  return (
    <SettingsSectionCard title="보안 환경 설정" description="계정 보안과 세션 관련 설정을 관리합니다.">
      <div className="flex flex-col gap-3">
        <SettingsSelectField
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
          <SettingsToggleSwitch
            id="toggle-session-keep"
            checked={settings.sessionKeep}
            onChange={(v) => onChange('sessionKeep', v)}
          />
        </div>
      </div>
    </SettingsSectionCard>
  )
}
