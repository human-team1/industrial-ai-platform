import type { ReactNode } from 'react'
import { formatDateTime } from '../../shared/lib/date'
import type { ChatHistorySidebarProps } from './types'

type SummaryTone = 'blue' | 'amber' | 'sky' | 'emerald' | 'violet'

export function ChatHistorySidebar({ summary, recent }: ChatHistorySidebarProps) {
  return (
    <aside className="space-y-4">
      <section className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <h3 className="text-sm font-semibold text-slate-800">히스토리 요약</h3>
        <ul className="mt-3 space-y-2">
          <SummaryRow
            tone="blue"
            icon={<IconChat />}
            label="전체 대화 수"
            value={`${(summary?.totalConversations ?? 0).toLocaleString()}건`}
          />
          <SummaryRow
            tone="amber"
            icon={<IconStar />}
            label="즐겨찾기"
            value={`${(summary?.favoriteCount ?? 0).toLocaleString()}건`}
            note="후속 API 연동 예정"
          />
          <SummaryRow
            tone="sky"
            icon={<IconClock />}
            label="최근 사용"
            value={summary?.lastActivityAt ? formatDateTime(summary.lastActivityAt) : '-'}
          />
          <SummaryRow
            tone="emerald"
            icon={<IconMessage />}
            label="현재 페이지 메시지 수"
            value={`${(summary?.pageMessageCount ?? 0).toLocaleString()}개`}
          />
          <SummaryRow
            tone="violet"
            icon={<IconDocument />}
            label="관련 문서 수"
            value={`${(summary?.pageRelatedSourceCount ?? 0).toLocaleString()}건`}
            note="현재 페이지 대화 기준"
          />
        </ul>
      </section>

      <section className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <h3 className="text-sm font-semibold text-slate-800">최근 활동</h3>
        <p className="mt-1 text-[11px] text-slate-500">현재 페이지 기준 상위 3건</p>
        <ul className="mt-3 space-y-2 text-sm">
          {recent.length === 0 ? (
            <li className="text-xs text-slate-500">표시할 활동이 없습니다.</li>
          ) : (
            recent.map((item) => (
              <li key={item.conversationId} className="rounded border border-slate-100 px-3 py-2">
                <p className="truncate text-sm font-medium text-slate-800">{item.title}</p>
                <p className="mt-0.5 text-[11px] text-slate-500">{formatDateTime(item.updatedAt)}</p>
              </li>
            ))
          )}
        </ul>
      </section>

      <section className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <h3 className="text-sm font-semibold text-slate-800">주요 태그</h3>
        <p className="mt-2 text-xs text-slate-500">태그 데이터는 후속 API 연동 예정입니다.</p>
      </section>
    </aside>
  )
}

function SummaryRow({
  tone,
  icon,
  label,
  value,
  note,
}: {
  tone: SummaryTone
  icon: ReactNode
  label: string
  value: string
  note?: string
}) {
  return (
    <li className="flex items-center gap-3">
      <span
        className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full ${iconBg(tone)} ${iconColor(tone)}`}
      >
        {icon}
      </span>
      <div className="min-w-0 flex-1">
        <p className="truncate text-xs text-slate-500">{label}</p>
        {note ? <p className="truncate text-[10px] text-slate-400">{note}</p> : null}
      </div>
      <span className="shrink-0 text-sm font-semibold text-slate-900">{value}</span>
    </li>
  )
}

function iconBg(tone: SummaryTone) {
  return {
    blue: 'bg-sky-50',
    amber: 'bg-amber-50',
    sky: 'bg-cyan-50',
    emerald: 'bg-emerald-50',
    violet: 'bg-violet-50',
  }[tone]
}

function iconColor(tone: SummaryTone) {
  return {
    blue: 'text-sky-500',
    amber: 'text-amber-500',
    sky: 'text-cyan-500',
    emerald: 'text-emerald-500',
    violet: 'text-violet-500',
  }[tone]
}

function IconChat() {
  return (
    <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 5h16v11H10l-4 4v-4H4z" />
    </svg>
  )
}

function IconStar() {
  return (
    <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 3l2.6 5.4 5.9.8-4.3 4.1 1 5.9L12 16.9 6.8 19.2l1-5.9L3.5 9.2l5.9-.8z" />
    </svg>
  )
}

function IconClock() {
  return (
    <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </svg>
  )
}

function IconMessage() {
  return (
    <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 12a8 8 0 0 1-11.6 7.1L4 21l1.9-5.4A8 8 0 1 1 21 12z" />
    </svg>
  )
}

function IconDocument() {
  return (
    <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M7 3h7l5 5v13H7z" />
      <path d="M14 3v5h5" />
      <path d="M9 13h6M9 17h6" />
    </svg>
  )
}
