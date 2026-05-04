import type { ResultListSummary } from '../../features/result/api/types'

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
            className="animate-pulse rounded-lg border border-slate-200 bg-white p-5 shadow-sm"
          >
            <div className="mb-4 h-9 w-9 rounded bg-slate-100" />
            <div className="h-4 w-24 rounded bg-slate-100" />
            <div className="mt-3 h-8 w-32 rounded bg-slate-100" />
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
  const avg =
    summary.avgScore === null || summary.avgScore === undefined
      ? '-'
      : Number(summary.avgScore as string | number).toFixed(2)

  return (
    <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
      <SummaryCard label="전체 결과" value={`${total.toLocaleString()}건`} tone="slate" />
      <SummaryCard
        label="정상"
        value={`${Number(summary.normalCount).toLocaleString()}건`}
        sub={ratio(Number(summary.normalCount), total)}
        tone="green"
      />
      <SummaryCard
        label="이상"
        value={`${Number(summary.defectCount).toLocaleString()}건`}
        sub={ratio(Number(summary.defectCount), total)}
        tone="red"
      />
      <SummaryCard label="평균 이상 점수" value={avg} tone="orange" />
    </section>
  )
}

function SummaryCard({
  label,
  value,
  sub,
  tone,
}: {
  label: string
  value: string
  sub?: string
  tone: 'slate' | 'green' | 'red' | 'orange'
}) {
  const toneClass = {
    slate: 'bg-slate-50 text-slate-700',
    green: 'bg-emerald-50 text-emerald-700',
    red: 'bg-red-50 text-red-700',
    orange: 'bg-orange-50 text-orange-700',
  }[tone]
  return (
    <article className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className={`mb-4 inline-flex h-9 w-9 items-center justify-center rounded ${toneClass}`}>▦</div>
      <p className="text-sm font-medium text-slate-500">{label}</p>
      <p className="mt-2 text-2xl font-bold text-slate-950">{value}</p>
      {sub ? <p className="mt-1 text-sm text-slate-500">{sub}</p> : null}
    </article>
  )
}

function ratio(count: number, total: number) {
  if (!total) return '0.0%'
  return `${((count / total) * 100).toFixed(1)}%`
}
