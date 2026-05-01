import { SettingsSectionCard } from './primitives'
import type { SummaryView } from '../types'

export function SettingsSummary({ settings }: { settings: SummaryView }) {
  const summaryItems = [
    { label: '기본 진입 페이지', value: settings.entryPage },
    { label: '새로고침 주기', value: settings.refreshCycle },
    { label: '차트 기간', value: settings.chartRange },
    { label: '테마', value: settings.theme },
    { label: '이상 탐지 알림', value: settings.anomalyAlert ? '켜짐' : '꺼짐' },
    { label: '이메일 알림', value: settings.emailAlert ? '켜짐' : '꺼짐' },
    {
      label: '탐지 임계값',
      value: Number.isFinite(settings.threshold) ? settings.threshold.toFixed(2) : '-',
    },
    { label: '자리 비움 자동 잠금', value: settings.autoLogout },
  ]

  return (
    <SettingsSectionCard title="설정 요약" description="현재 적용 중인 설정의 요약입니다.">
      <div className="grid grid-cols-2 gap-x-6 gap-y-2">
        {summaryItems.map((item) => (
          <div key={item.label} className="flex items-center justify-between py-1.5 border-b border-[#f5f6fa]">
            <span className="text-[#6b7280] text-[11px]">{item.label}</span>
            <span className="text-[#374151] text-[12px] font-medium">{item.value}</span>
          </div>
        ))}
      </div>
    </SettingsSectionCard>
  )
}
