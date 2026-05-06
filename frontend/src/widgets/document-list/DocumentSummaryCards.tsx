import type { ReactNode } from 'react'
import type { DocumentSummaryCardsProps } from './types'

type SummaryTone = 'blue' | 'rose' | 'indigo' | 'green' | 'amber' | 'red'

export function DocumentSummaryCards({ summary, loading }: DocumentSummaryCardsProps) {
  if (loading) {
    return (
      <section className="grid grid-cols-1 gap-4 md:grid-cols-3 xl:grid-cols-6">
        {[1, 2, 3, 4, 5, 6].map((key) => (
          <article key={key} className="animate-pulse rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <div className="grid grid-cols-3 items-center gap-3">
              <div className="col-span-1 flex justify-center">
                <div className="h-12 w-12 rounded-full bg-slate-100" />
              </div>
              <div className="col-span-2 space-y-2">
                <div className="h-3 w-16 rounded bg-slate-100" />
                <div className="h-6 w-20 rounded bg-slate-100" />
              </div>
            </div>
          </article>
        ))}
      </section>
    )
  }

  if (!summary) {
    return (
      <section className="rounded-lg border border-slate-200 bg-slate-50 p-4 text-sm text-slate-700">
        <p className="font-medium text-slate-900">집계 정보를 불러올 수 없습니다.</p>
        <p className="mt-1 text-slate-600">
          현재 페이지 기준 집계가 없거나 목록을 불러오지 못했습니다. 카드에 0으로 채우지 않습니다.
        </p>
      </section>
    )
  }

  const total = Number(summary.totalCount) || 0

  return (
    <section className="grid grid-cols-1 gap-4 md:grid-cols-3 xl:grid-cols-6">
      <Card tone="blue" icon={<IconDocument />} label="전체" value={summary.totalCount} sub="전체 100%" />
      <Card tone="rose" icon={<IconPdf />} label="PDF" value={summary.pdfCount} sub={`전체의 ${ratio(summary.pdfCount, total)}`} />
      <Card tone="indigo" icon={<IconDocx />} label="DOCX" value={summary.docxCount} sub={`전체의 ${ratio(summary.docxCount, total)}`} />
      <Card tone="green" icon={<IconCheck />} label="반영완료" value={summary.completedCount} sub={`전체의 ${ratio(summary.completedCount, total)}`} />
      <Card tone="amber" icon={<IconClock />} label="처리중" value={summary.processingCount} sub={`전체의 ${ratio(summary.processingCount, total)}`} />
      <Card tone="red" icon={<IconX />} label="반영실패" value={summary.failedCount} sub={`전체의 ${ratio(summary.failedCount, total)}`} />
    </section>
  )
}

function ratio(count: number, total: number) {
  if (!total) return '0.0%'
  return `${((Number(count) / total) * 100).toFixed(1)}%`
}

function Card({
  tone,
  icon,
  label,
  value,
  sub,
}: {
  tone: SummaryTone
  icon: ReactNode
  label: string
  value: number
  sub?: string
}) {
  return (
    <article className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="grid grid-cols-3 items-center gap-3">
        <div className="col-span-1 flex justify-center">
          <span className={`flex h-12 w-12 items-center justify-center rounded-full ${iconBg(tone)} ${iconColor(tone)}`}>
            {icon}
          </span>
        </div>
        <div className="col-span-2 min-w-0">
          <p className="text-sm font-medium text-slate-500">{label}</p>
          <p className="mt-1 truncate text-xl font-bold text-slate-950">{value.toLocaleString()}</p>
          {sub ? <p className="mt-1 text-xs text-slate-500">{sub}</p> : null}
        </div>
      </div>
    </article>
  )
}

function iconBg(tone: SummaryTone) {
  return {
    blue: 'bg-sky-50',
    rose: 'bg-rose-50',
    indigo: 'bg-indigo-50',
    green: 'bg-emerald-50',
    amber: 'bg-amber-50',
    red: 'bg-red-50',
  }[tone]
}

function iconColor(tone: SummaryTone) {
  return {
    blue: 'text-sky-500',
    rose: 'text-rose-500',
    indigo: 'text-indigo-500',
    green: 'text-emerald-500',
    amber: 'text-amber-500',
    red: 'text-red-500',
  }[tone]
}

function IconDocument() {
  return (
    <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M7 3h7l5 5v13H7z" />
      <path d="M14 3v5h5" />
      <path d="M9 13h6M9 17h6" />
    </svg>
  )
}

function IconPdf() {
  return (
    <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M7 3h7l5 5v13H7z" />
      <path d="M14 3v5h5" />
      <text x="9" y="17" fontSize="5" fontWeight="700" fill="currentColor" stroke="none">PDF</text>
    </svg>
  )
}

function IconDocx() {
  return (
    <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M7 3h7l5 5v13H7z" />
      <path d="M14 3v5h5" />
      <text x="8.2" y="17" fontSize="4.2" fontWeight="700" fill="currentColor" stroke="none">DOC</text>
    </svg>
  )
}

function IconCheck() {
  return (
    <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="9" />
      <path d="M8 12.5l3 3 5-6" />
    </svg>
  )
}

function IconClock() {
  return (
    <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </svg>
  )
}

function IconX() {
  return (
    <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="9" />
      <path d="M9 9l6 6M15 9l-6 6" />
    </svg>
  )
}
