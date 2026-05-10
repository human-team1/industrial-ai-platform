import type { ReactNode } from 'react'
import type { BarChartViewModel, EmptyChartState } from '../../lib/chartViewModel'

type Props = {
  viewModel: BarChartViewModel | null
  loading?: boolean
  error?: string | null
  empty?: EmptyChartState
}

export function BarChartCard({ viewModel, loading, error, empty }: Props) {
  if (loading) return <ChartShell title={viewModel?.title ?? '차트'}><div className="h-32 animate-pulse rounded bg-slate-100" /></ChartShell>
  if (error) return <ChartShell title={viewModel?.title ?? '차트'}><StateText tone="error" text={error} /></ChartShell>
  if (!viewModel || viewModel.points.length === 0) {
    return <ChartShell title={viewModel?.title ?? empty?.message ?? '차트'}><StateText text={empty?.message ?? '표시할 데이터가 없습니다.'} /></ChartShell>
  }

  const values = viewModel.points.map((point) => point.value)
  const max = Math.max(...values, 0)
  const scaleMax = max <= 0 ? 1 : max

  return (
    <ChartShell title={viewModel.title} caption={viewModel.caption}>
      {values.length === 0 ? (
        <StateText text="표시할 숫자 데이터가 없습니다." />
      ) : (
        <div className="flex h-full flex-col justify-between" role="img" aria-label={`${viewModel.title} 막대 그래프`}>
          {viewModel.points.slice(0, 10).map((point) => {
            const width = point.value === 0 ? 3 : Math.max(3, Math.round((point.value / scaleMax) * 100))
            return (
              <div key={point.label} className="grid grid-cols-[minmax(86px,0.8fr)_minmax(120px,1.5fr)_auto] items-center gap-3 text-xs">
                <span className="truncate text-slate-600" title={point.label}>{point.label}</span>
                <span className="h-2.5 overflow-hidden rounded-full bg-slate-100">
                  <span className="block h-full rounded-full bg-[#109498]" style={{ width: `${width}%` }} />
                </span>
                <span className="font-semibold text-slate-800">
                  {point.value.toLocaleString()}
                  {viewModel.unit ?? ''}
                </span>
              </div>
            )
          })}
        </div>
      )}
    </ChartShell>
  )
}

function ChartShell({ title, caption, children }: { title: string; caption?: string; children: ReactNode }) {
  return (
    <section className="flex h-full flex-col rounded border border-slate-200 bg-white p-[15px] shadow-sm">
      <div className="flex shrink-0 items-start justify-between gap-3">
        <h2 className="text-base font-semibold text-slate-900">{title}</h2>
        {caption ? <span className="text-xs text-slate-400">{caption}</span> : null}
      </div>
      <div className="mt-4 min-h-0 flex-1">{children}</div>
    </section>
  )
}

function StateText({ text, tone = 'empty' }: { text: string; tone?: 'empty' | 'error' }) {
  return <p className={`py-8 text-center text-sm ${tone === 'error' ? 'text-rose-600' : 'text-slate-500'}`}>{text}</p>
}
