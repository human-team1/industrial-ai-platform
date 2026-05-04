import type { ReactNode } from 'react'
import type { ResultListSummary } from '../../features/result/api/types'

type Tone = 'blue' | 'green' | 'orange' | 'violet'

type Props = {
  summary: ResultListSummary | null
  loading?: boolean
}

export function ResultSummaryCards({ summary, loading }: Props) {
  if (loading) {
    return (
      <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        {[1, 2, 3, 4].map((key) => (
          <article
            key={key}
            className="animate-pulse rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
          >
            <div className="grid grid-cols-3 items-center gap-4">
              <div className="col-span-1 flex justify-center">
                <div className="h-[72px] w-[72px] rounded-full bg-slate-100" />
              </div>
              <div className="col-span-2 space-y-2">
                <div className="h-4 w-20 rounded bg-slate-100" />
                <div className="h-7 w-24 rounded bg-slate-100" />
                <div className="h-3 w-16 rounded bg-slate-100" />
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
          `GET /results` 응답에 `summary`가 없거나 아직 제공되지 않습니다. 수치로 0을 표시하지 않습니다.
        </p>
      </section>
    )
  }

  const total = Number(summary.totalCount) || 0
  const normalCount = Number(summary.normalCount) || 0
  const defectCount = Number(summary.defectCount) || 0
  const avg =
    summary.avgScore === null || summary.avgScore === undefined
      ? '-'
      : Number(summary.avgScore as string | number).toFixed(2)

  return (
    <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
      <SummaryCard
        tone="blue"
        icon={<IconDocument />}
        label="전체 결과"
        value={`${total.toLocaleString()}건`}
        sub="전체 100%"
      />
      <SummaryCard
        tone="green"
        icon={<IconShield />}
        label="정상"
        value={`${normalCount.toLocaleString()}건`}
        sub={`전체의 ${ratio(normalCount, total)}`}
      />
      <SummaryCard
        tone="orange"
        icon={<IconWarning />}
        label="이상"
        value={`${defectCount.toLocaleString()}건`}
        sub={`전체의 ${ratio(defectCount, total)}`}
      />
      <SummaryCard
        tone="violet"
        icon={<IconGauge />}
        label="평균 이상 점수"
        value={avg}
        sub="0~1 범위"
      />
    </section>
  )
}

function SummaryCard({
  tone,
  icon,
  label,
  value,
  sub,
}: {
  tone: Tone
  icon: ReactNode
  label: string
  value: string
  sub?: string
}) {
  return (
    <article className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="grid grid-cols-3 items-center gap-4">
        <div className="col-span-1 flex justify-center">
          <span
            className={`flex h-[72px] w-[72px] items-center justify-center rounded-full ${iconBg(tone)} ${iconColor(tone)}`}
          >
            {icon}
          </span>
        </div>
        <div className="col-span-2 min-w-0">
          <p className="text-sm font-medium text-slate-500">{label}</p>
          <p className="mt-1 truncate text-2xl font-bold text-slate-950">{value}</p>
          {sub ? <p className="mt-1 text-xs text-slate-500">{sub}</p> : null}
        </div>
      </div>
    </article>
  )
}

function iconBg(tone: Tone) {
  return {
    blue: 'bg-sky-50',
    green: 'bg-emerald-50',
    orange: 'bg-amber-50',
    violet: 'bg-violet-50',
  }[tone]
}

function iconColor(tone: Tone) {
  return {
    blue: 'text-sky-500',
    green: 'text-emerald-500',
    orange: 'text-amber-500',
    violet: 'text-violet-500',
  }[tone]
}

function ratio(count: number, total: number) {
  if (!total) return '0.0%'
  return `${((count / total) * 100).toFixed(1)}%`
}

function IconDocument() {
  return (
    <svg
      className="h-9 w-9"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M7 3h7l5 5v13H7z" />
      <path d="M14 3v5h5" />
      <path d="M9 13h6M9 17h6" />
    </svg>
  )
}

function IconShield() {
  return (
    <svg
      className="h-9 w-9"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M12 3l8 3v6c0 4.5-3.4 8.4-8 9-4.6-.6-8-4.5-8-9V6l8-3z" />
      <path d="M9 12l2 2 4-4" />
    </svg>
  )
}

function IconWarning() {
  return (
    <svg
      className="h-9 w-9"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M12 4 2.5 20h19L12 4z" />
      <path d="M12 10v4" />
      <circle cx="12" cy="17" r="0.6" fill="currentColor" />
    </svg>
  )
}

function IconGauge() {
  return (
    <svg
      className="h-9 w-9"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M4 14a8 8 0 1 1 16 0" />
      <path d="M12 14l4-3" />
      <circle cx="12" cy="14" r="1" fill="currentColor" />
    </svg>
  )
}
