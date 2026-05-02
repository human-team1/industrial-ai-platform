import type { ReactNode } from 'react'
import type { EmptyChartState, LineChartViewModel } from '../../lib/chartViewModel'

type Props = {
  viewModel: LineChartViewModel | null
  loading?: boolean
  error?: string | null
  empty?: EmptyChartState
}

const COLORS = ['#109498', '#dc2626', '#f59e0b', '#2563eb']

export function LineChartCard({ viewModel, loading, error, empty }: Props) {
  if (loading) {
    return (
      <ChartShell title={viewModel?.title ?? '차트'}>
        <div className="h-48 animate-pulse rounded bg-slate-100" />
      </ChartShell>
    )
  }

  if (error) {
    return (
      <ChartShell title={viewModel?.title ?? '차트'}>
        <StateText tone="error" text={error} />
      </ChartShell>
    )
  }

  if (!viewModel || viewModel.points.length === 0) {
    return (
      <ChartShell title={viewModel?.title ?? '차트'}>
        <StateText text={empty?.message ?? '표시할 데이터가 없습니다.'} />
      </ChartShell>
    )
  }

  const numericValues = viewModel.points.flatMap((point) =>
    viewModel.series.map((series) => point[series.key]).filter((value): value is number => typeof value === 'number'),
  )
  const dataMax = Math.max(...numericValues, 0)
  const scaleMax = viewModel.yMax && viewModel.yMax > 0 ? viewModel.yMax : dataMax <= 0 ? 1 : dataMax

  return (
    <ChartShell title={viewModel.title} caption={viewModel.caption}>
      {numericValues.length === 0 ? (
        <StateText text="표시할 숫자 데이터가 없습니다." />
      ) : (
        <div role="img" aria-label={`${viewModel.title} 선 그래프`}>
          <svg viewBox="0 0 640 220" className="h-56 w-full overflow-visible">
            <line x1="36" y1="184" x2="620" y2="184" stroke="#e2e8f0" />
            <line x1="36" y1="24" x2="36" y2="184" stroke="#e2e8f0" />
            <text x="28" y="30" textAnchor="end" className="fill-slate-400 text-[10px]">
              {scaleMax.toLocaleString()}
            </text>
            <text x="28" y="188" textAnchor="end" className="fill-slate-400 text-[10px]">
              0
            </text>
            {viewModel.series.map((series, index) =>
              toPolylineSegments(viewModel, series.key, scaleMax).map((points, segmentIndex) => (
                <polyline
                  key={`${series.key}-${segmentIndex}`}
                  fill="none"
                  stroke={COLORS[index % COLORS.length]}
                  strokeWidth="2.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  points={points}
                />
              )),
            )}
            {viewModel.points.map((point, index) => (
              <text
                key={`${point.time}-${index}`}
                x={x(index, viewModel.points.length)}
                y="206"
                textAnchor="middle"
                className="fill-slate-400 text-[10px]"
              >
                {String(point[viewModel.xKey] ?? '').slice(-5)}
              </text>
            ))}
          </svg>
          <div className="mt-2 flex flex-wrap gap-3 text-xs text-slate-500">
            {viewModel.series.map((series, index) => (
              <span key={series.key} className="flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full" style={{ backgroundColor: COLORS[index % COLORS.length] }} />
                {series.label}
                {series.unit ? ` (${series.unit})` : ''}
              </span>
            ))}
          </div>
        </div>
      )}
    </ChartShell>
  )
}

function toPolylineSegments(viewModel: LineChartViewModel, key: string, max: number) {
  const segments: string[][] = []
  let current: string[] = []

  viewModel.points.forEach((point, index) => {
    const value = point[key]
    if (typeof value !== 'number') {
      if (current.length > 0) {
        segments.push(current)
        current = []
      }
      return
    }
    const bounded = Math.max(0, Math.min(max, value))
    current.push(`${x(index, viewModel.points.length)},${184 - (bounded / max) * 160}`)
  })

  if (current.length > 0) segments.push(current)
  return segments.map((segment) => segment.join(' '))
}

function x(index: number, length: number) {
  if (length <= 1) return 36
  return 36 + (index / (length - 1)) * 584
}

function ChartShell({ title, caption, children }: { title: string; caption?: string; children: ReactNode }) {
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <h2 className="text-base font-semibold text-slate-900">{title}</h2>
        {caption ? <span className="text-xs text-slate-400">{caption}</span> : null}
      </div>
      <div className="mt-4">{children}</div>
    </section>
  )
}

function StateText({ text, tone = 'empty' }: { text: string; tone?: 'empty' | 'error' }) {
  return <p className={`py-8 text-center text-sm ${tone === 'error' ? 'text-rose-600' : 'text-slate-500'}`}>{text}</p>
}
