import { SettingsSectionCard, SettingsSelectField, SettingsToggleSwitch } from './primitives'
import { AUTO_LOGOUT_OPTIONS, type SecurityView } from '../types'

// 주의: 이 섹션의 항목은 화면 표시 환경(localStorage)이며, 실제 인증 세션 만료/refresh/revoke 정책에는 영향이 없다.
export function SecurityPreferencesSection({
  settings,
  onChange,
}: {
  settings: SecurityView
  onChange: <K extends keyof SecurityView>(key: K, value: SecurityView[K]) => void
}) {
  return (
    <SettingsSectionCard
      title="보안 화면 설정"
      description="화면에서의 표시 동작입니다. 실제 로그인 세션 정책은 별도 관리됩니다."
    >
      <div className="flex flex-col gap-3">
        <SettingsSelectField
          id="auto-logout"
          label="자리 비움 자동 잠금"
          value={settings.autoLogout}
          options={AUTO_LOGOUT_OPTIONS}
          onChange={(v) => onChange('autoLogout', v)}
        />
        <div className="flex items-center justify-between gap-4">
          <div>
            <div className="text-[#374151] text-[13px] mb-0.5">로그인 화면 자동 채움</div>
            <div className="text-[#6b7280] text-[11px]">
              이 브라우저에서 로그인 화면 입력값을 기억합니다. 세션 자체는 유지하지 않습니다.
            </div>
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
