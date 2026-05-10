import type { ChartSeries, ParsedTable } from '../../lib/structuredText'

export type StructuredTextPreviewViewModel =
  | {
      kind: 'parsed'
      title: string
      sourceFormat: string
      table?: ParsedTable
      chart?: ChartSeries
    }
  | {
      kind: 'fallback'
      title: string
      reason: string
      text: string
    }
  | {
      kind: 'empty'
      title: string
    }

export function StructuredTextPreview({ viewModel }: { viewModel: StructuredTextPreviewViewModel }) {
  if (viewModel.kind === 'empty') {
    return <p className="mt-3 rounded border border-slate-200 bg-white px-3 py-2 text-xs text-slate-500">표시할 데이터가 없습니다.</p>
  }

  if (viewModel.kind === 'fallback') {
    return (
      <div className="mt-3 rounded border border-amber-100 bg-amber-50 p-3">
        <p className="text-xs font-semibold text-amber-800">그래프 변환이 어려운 형식입니다.</p>
        <p className="mt-1 text-xs text-amber-700">{viewModel.reason}</p>
        <pre className="mt-2 max-h-32 overflow-auto whitespace-pre-wrap break-words rounded bg-white p-2 text-xs text-slate-700">
          {viewModel.text}
        </pre>
      </div>
    )
  }

  return (
    <div className="mt-3 space-y-3 rounded border border-slate-200 bg-white p-3">
      <div className="flex items-center justify-between gap-2">
        <p className="text-xs font-semibold text-slate-700">{viewModel.title}</p>
        <span className="rounded bg-slate-100 px-2 py-1 text-[11px] font-medium text-slate-600">{viewModel.sourceFormat}</span>
      </div>
      {viewModel.chart ? <BarChart series={viewModel.chart} /> : null}
      {viewModel.table ? <ParsedTableView table={viewModel.table} /> : null}
    </div>
  )
}

function BarChart({ series }: { series: ChartSeries }) {
  const max = Math.max(...series.points.map((point) => point.value), 0)
  if (series.points.length === 0 || max <= 0) {
    return <p className="rounded bg-slate-50 p-3 text-xs text-slate-500">차트로 표시할 숫자 데이터가 없습니다.</p>
  }

  return (
    <div className="space-y-2" role="img" aria-label={`${series.name} 막대 그래프`}>
      {series.points.slice(0, 8).map((point) => {
        const width = Math.max(4, Math.round((point.value / max) * 100))
        return (
          <div key={point.label} className="grid grid-cols-[minmax(88px,0.8fr)_minmax(120px,1.5fr)_auto] items-center gap-2 text-xs">
            <span className="truncate text-slate-600" title={point.label}>{point.label}</span>
            <span className="h-2 overflow-hidden rounded-full bg-slate-100">
              <span className="block h-full rounded-full bg-[#109498]" style={{ width: `${width}%` }} />
            </span>
            <span className="font-semibold text-slate-700">{point.value.toLocaleString()}</span>
          </div>
        )
      })}
    </div>
  )
}

function ParsedTableView({ table }: { table: ParsedTable }) {
  if (table.rows.length === 0) {
    return <p className="rounded bg-slate-50 p-3 text-xs text-slate-500">표시할 행이 없습니다.</p>
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-left text-xs">
        <thead className="text-slate-500">
          <tr>
            {table.columns.map((column) => (
              <th key={column} className="border-b border-slate-100 px-2 py-2 font-medium">{column}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {table.rows.slice(0, 6).map((row, index) => (
            <tr key={index} className="border-b border-slate-50 last:border-0">
              {table.columns.map((column) => (
                <td key={column} className="max-w-[180px] truncate px-2 py-2 text-slate-700" title={String(row[column] ?? '')}>
                  {row[column] ?? '-'}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
