import { Link } from 'react-router-dom'
import type {
  DashboardOverview,
  DashboardTrendPoint,
  RecentDashboardNotification,
  RecentDashboardResult,
  TopEquipmentAnomalyRate,
} from '../../entities/dashboard/model/types'
import {
  toDashboardEquipmentChart,
} from '../../features/dashboard/model/chartViewModels'
import { formatDate, formatDateTime } from '../../shared/lib/date'
import { BarChartCard } from '../../shared/ui/chart/BarChartCard'

type Props = {
  data: DashboardOverview
}

export function DashboardOverviewView({ data }: Props) {
  return (
    <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,4fr)_minmax(0,1fr)]">
      <div className="space-y-5">
        <KpiGrid data={data} />
        <div className="grid grid-cols-1 gap-5 lg:grid-cols-2 [&>section]:h-[300px] [&>section]:overflow-hidden">
          <TrendComboChart trend={data.trend} />
          <BarChartCard viewModel={toDashboardEquipmentChart(data)} empty={{ reason: 'NO_DATA', message: '표시할 데이터가 없습니다.' }} />
        </div>
        <RecentResultsTable rows={data.recentResults} />
      </div>

      <aside className="space-y-5">
        <RecentNotifications rows={data.recentNotifications} />
        <SummaryPanel data={data} />
      </aside>
    </div>
  )
}

function KpiGrid({ data }: Props) {
  const { kpis } = data
  const cards = [
    {
      label: '전체 검사 건수',
      value: `${kpis.totalInspectionCount.toLocaleString()} 건`,
      change: kpis.totalInspectionChangeRate,
      tone: 'blue',
      icon: <KpiIconPulse />,
    },
    {
      label: '이상 탐지 건수',
      value: `${kpis.anomalyCount.toLocaleString()} 건`,
      change: kpis.anomalyChangeRate,
      tone: 'orange',
      icon: <KpiIconWarning />,
    },
    {
      label: '정상 건수',
      value: `${kpis.normalCount.toLocaleString()} 건`,
      change: kpis.normalChangeRate,
      tone: 'green',
      icon: <KpiIconShield />,
    },
    {
      label: '이상 탐지율',
      value: `${kpis.anomalyRate.toFixed(2)} %`,
      change: kpis.anomalyRateChangePoint,
      tone: 'amber',
      changeSuffix: 'p',
      icon: <KpiIconPercent />,
    },
  ]

  return (
    <section className="rounded border border-slate-200 bg-white shadow-sm">
      <div className="grid grid-cols-1 divide-y divide-slate-100 md:grid-cols-2 md:divide-y-0 md:[&>*:nth-child(n+3)]:border-t md:[&>*:nth-child(n+3)]:border-slate-100 md:[&>*:nth-child(2n)]:border-l md:[&>*:nth-child(2n)]:border-slate-100 xl:grid-cols-4 xl:[&>*:nth-child(n+3)]:border-t-0 xl:[&>*:nth-child(2n)]:border-l-0 xl:[&>*+*]:border-l xl:[&>*+*]:border-slate-100">
        {cards.map((card) => (
          <div key={card.label} className="flex items-center gap-4 p-2.5">
            <span
              className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-full ${iconTone(card.tone)} ${iconColor(card.tone)}`}
            >
              {card.icon}
            </span>
            <div className="min-w-0">
              <p className="text-sm font-medium text-slate-500">{card.label}</p>
              <p className="mt-1 text-2xl font-bold text-slate-900">{card.value}</p>
              <p className="mt-1 flex items-center gap-1 text-xs font-medium">
                <span className="text-slate-400">지난 7일 대비</span>
                <span
                  className={`flex items-center gap-0.5 ${card.change >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}
                >
                  <span aria-hidden="true">{card.change >= 0 ? '▲' : '▼'}</span>
                  <span>
                    {card.change >= 0 ? '+' : ''}
                    {card.change.toFixed(2)}
                    {card.changeSuffix ?? '%'}
                  </span>
                </span>
              </p>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

function KpiIconPulse() {
  return (
    <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 12h4l2-6 4 12 2-6h6" />
    </svg>
  )
}

function KpiIconWarning() {
  return (
    <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 4 2.5 20h19L12 4z" />
      <path d="M12 10v4" />
      <circle cx="12" cy="17" r="0.6" fill="currentColor" />
    </svg>
  )
}

function KpiIconShield() {
  return (
    <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 3l8 3v6c0 4.5-3.4 8.4-8 9-4.6-.6-8-4.5-8-9V6l8-3z" />
      <path d="M9 12l2 2 4-4" />
    </svg>
  )
}

function KpiIconPercent() {
  return (
    <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 5 5 19" />
      <circle cx="7.5" cy="7.5" r="2.5" />
      <circle cx="16.5" cy="16.5" r="2.5" />
    </svg>
  )
}

function TrendComboChart({ trend }: { trend: DashboardTrendPoint[] }) {
  const rows = trend.map((p) => {
    const total = p.normalCount + p.anomalyCount + p.recheckCount
    const parts = p.date.split('-')
    return {
      date: parts.length === 3 ? `${parts[1]}/${parts[2]}` : p.date,
      total,
      anomaly: p.anomalyCount,
      normal: total - p.anomalyCount,
      rate: p.anomalyRate,
    }
  })

  if (rows.length === 0) {
    return (
      <section className="flex h-full flex-col rounded border border-slate-200 bg-white p-[15px] shadow-sm">
        <div className="flex shrink-0 items-start justify-between gap-3">
          <h2 className="text-base font-semibold text-slate-900">이상 탐지 추이</h2>
        </div>
        <div className="flex flex-1 items-center justify-center text-sm text-slate-500">
          표시할 데이터가 없습니다.
        </div>
      </section>
    )
  }

  const VB_W = 700
  const VB_H = 400
  const PAD_L = 60
  const PAD_R = 60
  const PAD_T = 30
  const PAD_B = 50
  const plotW = VB_W - PAD_L - PAD_R
  const plotH = VB_H - PAD_T - PAD_B

  const countMax = Math.ceil(Math.max(...rows.map((r) => r.total)) / 50) * 50 || 50
  const rateMax = Math.max(10, Math.ceil(Math.max(...rows.map((r) => r.rate)) / 5) * 5)

  const xAt = (i: number) => PAD_L + ((i + 0.5) * plotW) / rows.length
  const yCount = (v: number) => PAD_T + plotH - (v / countMax) * plotH
  const yRate = (v: number) => PAD_T + plotH - (v / rateMax) * plotH
  const barW = (plotW / rows.length) * 0.367

  const linePoints = rows.map((r, i) => ({ x: xAt(i), y: yRate(r.rate) }))
  const linePath = monotonePath(linePoints)

  return (
    <section className="flex h-full flex-col rounded border border-slate-200 bg-white p-[15px] shadow-sm">
      <div className="flex shrink-0 items-start justify-between gap-3">
        <h2 className="text-base font-semibold text-slate-900">이상 탐지 추이</h2>
        <div className="flex gap-3 text-[13px] text-slate-500">
          <Legend color="bg-emerald-500" label="정상" />
          <Legend color="bg-orange-400" label="이상" />
          <Legend color="bg-sky-500" label="이상 탐지율" />
        </div>
      </div>

      <div className="mt-2 min-h-0 flex-1">
        <svg viewBox={`0 0 ${VB_W} ${VB_H}`} preserveAspectRatio="xMidYMid meet" className="h-full w-full">
          {[0, 0.25, 0.5, 0.75, 1].map((t) => {
            const y = PAD_T + plotH * (1 - t)
            return (
              <g key={t}>
                <line x1={PAD_L} y1={y} x2={VB_W - PAD_R} y2={y} stroke="#eef2f7" />
                <text x={PAD_L - 8} y={y + 7} textAnchor="end" className="fill-slate-500 text-[20px]">
                  {Math.round(countMax * t)}
                </text>
                <text x={VB_W - PAD_R + 8} y={y + 7} textAnchor="start" className="fill-slate-500 text-[20px]">
                  {Math.round(rateMax * t)}%
                </text>
              </g>
            )
          })}

          {rows.map((r, i) => {
            const x = xAt(i) - barW / 2
            const yAnomalyTop = yCount(r.anomaly)
            const anomalyH = PAD_T + plotH - yAnomalyTop
            const yNormalTop = yCount(r.total)
            const normalH = yAnomalyTop - yNormalTop
            return (
              <g key={r.date}>
                <rect x={x} y={yAnomalyTop} width={barW} height={Math.max(0, anomalyH)} fill="#fb923c" rx={2} />
                <rect x={x} y={yNormalTop} width={barW} height={Math.max(0, normalH)} fill="#34d399" rx={2} />
              </g>
            )
          })}

          <path d={linePath} fill="none" stroke="#0ea5e9" strokeWidth={2.6} strokeLinecap="round" strokeLinejoin="round" />
          {linePoints.map((p, i) => (
            <g key={i}>
              <circle cx={p.x} cy={p.y} r={4} fill="#fff" stroke="#0ea5e9" strokeWidth={2} />
              <text x={p.x} y={p.y - 12} textAnchor="middle" className="fill-sky-600 text-[16px] font-semibold">
                {rows[i].rate.toFixed(1)}%
              </text>
            </g>
          ))}

          {rows.map((r, i) => (
            <text
              key={r.date}
              x={xAt(i)}
              y={VB_H - PAD_B / 2 + 9}
              textAnchor="middle"
              className="fill-slate-600 text-[20px]"
            >
              {r.date}
            </text>
          ))}
        </svg>
      </div>
    </section>
  )
}

function monotonePath(pts: { x: number; y: number }[]) {
  if (pts.length === 0) return ''
  if (pts.length === 1) return `M ${pts[0].x} ${pts[0].y}`
  if (pts.length === 2) return `M ${pts[0].x} ${pts[0].y} L ${pts[1].x} ${pts[1].y}`

  const n = pts.length
  const dx: number[] = []
  const delta: number[] = []
  for (let i = 0; i < n - 1; i++) {
    dx.push(pts[i + 1].x - pts[i].x)
    delta.push((pts[i + 1].y - pts[i].y) / dx[i])
  }

  const m: number[] = new Array(n)
  m[0] = delta[0]
  m[n - 1] = delta[n - 2]
  for (let i = 1; i < n - 1; i++) {
    m[i] = delta[i - 1] * delta[i] <= 0 ? 0 : (delta[i - 1] + delta[i]) / 2
  }

  for (let i = 0; i < n - 1; i++) {
    if (delta[i] === 0) {
      m[i] = 0
      m[i + 1] = 0
      continue
    }
    const a = m[i] / delta[i]
    const b = m[i + 1] / delta[i]
    const h = a * a + b * b
    if (h > 9) {
      const t = 3 / Math.sqrt(h)
      m[i] = t * a * delta[i]
      m[i + 1] = t * b * delta[i]
    }
  }

  let d = `M ${pts[0].x} ${pts[0].y}`
  for (let i = 0; i < n - 1; i++) {
    const cp1x = pts[i].x + dx[i] / 3
    const cp1y = pts[i].y + (m[i] * dx[i]) / 3
    const cp2x = pts[i + 1].x - dx[i] / 3
    const cp2y = pts[i + 1].y - (m[i + 1] * dx[i]) / 3
    d += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${pts[i + 1].x} ${pts[i + 1].y}`
  }
  return d
}

function TrendChart({ rows }: { rows: DashboardTrendPoint[] }) {
  const maxCount = Math.max(1, ...rows.map((row) => row.normalCount + row.anomalyCount + row.recheckCount))

  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="text-base font-semibold text-slate-900">이상 탐지 추이</h2>
      {rows.length === 0 ? (
        <EmptyState />
      ) : (
        <div className="mt-5 flex h-[260px] items-end gap-3">
          {rows.map((row) => (
            <div key={row.date} className="flex min-w-0 flex-1 flex-col items-center gap-2">
              <div className="flex h-[190px] w-full items-end justify-center gap-1">
                <Bar value={row.normalCount} max={maxCount} className="bg-emerald-500" />
                <Bar value={row.anomalyCount} max={maxCount} className="bg-rose-500" />
                <Bar value={row.recheckCount} max={maxCount} className="bg-amber-400" />
              </div>
              <span className="text-[11px] text-slate-500">{row.date.slice(5)}</span>
              <span className="text-[11px] font-semibold text-slate-700">{row.anomalyRate.toFixed(1)}%</span>
            </div>
          ))}
        </div>
      )}
      <div className="mt-4 flex gap-4 text-xs text-slate-500">
        <Legend color="bg-emerald-500" label="정상" />
        <Legend color="bg-rose-500" label="이상" />
        <Legend color="bg-amber-400" label="재검사" />
      </div>
    </section>
  )
}

function TopEquipmentChart({ rows }: { rows: TopEquipmentAnomalyRate[] }) {
  const maxRate = Math.max(1, ...rows.map((row) => row.anomalyRate))
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="text-base font-semibold text-slate-900">설비별 이상 탐지율</h2>
      <div className="mt-5 space-y-4">
        {rows.length === 0 ? <EmptyState /> : null}
        {rows.map((row) => (
          <div key={`${row.targetId}-${row.equipmentName}`}>
            <div className="mb-1 flex items-center justify-between text-xs">
              <span className="font-medium text-slate-700">{row.equipmentName}</span>
              <span className="text-slate-500">{row.anomalyRate.toFixed(2)}%</span>
            </div>
            <div className="h-2 rounded bg-slate-100">
              <div className="h-2 rounded bg-[#109498]" style={{ width: `${Math.min(100, (row.anomalyRate / maxRate) * 100)}%` }} />
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

function RecentNotifications({ rows }: { rows: RecentDashboardNotification[] }) {
  return (
    <section className="flex h-[320px] flex-col rounded border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex shrink-0 items-center justify-between">
        <h2 className="text-base font-semibold text-slate-900">실시간 알림</h2>
        <Link to="/notifications" className="text-xs font-medium text-[#109498]">전체 보기</Link>
      </div>
      <div className="mt-4 min-h-0 flex-1 space-y-3 overflow-y-auto">
        {rows.length === 0 ? <EmptyState /> : null}
        {rows.map((item) => (
          <Link
            key={item.notificationId}
            to={item.targetUrl ?? '/dashboard'}
            className="block rounded border border-slate-100 bg-slate-50 p-3 transition-colors hover:bg-slate-100"
          >
            <div className="flex gap-3">
              <span className={`mt-0.5 h-2.5 w-2.5 shrink-0 rounded-full ${severityColor(item.severity)}`} />
              <div className="min-w-0">
                <p className="truncate text-sm font-medium text-slate-800">{item.title}</p>
                <p className="mt-1 text-xs text-slate-500">{formatDateTime(item.createdAt)}</p>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </section>
  )
}

function SummaryPanel({ data }: Props) {
  const summary = data.summary
  const items = [
    {
      icon: <SummaryIconServer />,
      tone: 'bg-sky-50 text-sky-500',
      label: '등록 설비 수',
      value: `${summary.registeredTargetCount.toLocaleString()}개`,
    },
    {
      icon: <SummaryIconChip />,
      tone: 'bg-emerald-50 text-emerald-500',
      label: '활성 모델 수',
      value: `${summary.activeModelCount.toLocaleString()}개`,
    },
    {
      icon: <SummaryIconStorage />,
      tone: 'bg-violet-50 text-violet-500',
      label: '총 데이터 용량',
      value: formatBytes(summary.totalDataSizeBytes),
    },
    {
      icon: <SummaryIconCalendar />,
      tone: 'bg-amber-50 text-amber-500',
      label: '최근 학습 일자',
      value: formatDate(summary.latestTrainingDate ?? undefined),
    },
  ]

  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="text-base font-semibold text-slate-900">요약 정보</h2>
      <div className="mt-4 flex flex-col gap-2">
        {items.map((item) => (
          <div
            key={item.label}
            className="flex items-center gap-3 rounded bg-slate-50 p-0"
          >
            <span className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full ${item.tone}`}>
              {item.icon}
            </span>
            <span className="flex-1 text-xs text-slate-500">{item.label}</span>
            <span className="text-sm font-semibold text-slate-900">{item.value}</span>
          </div>
        ))}
      </div>
    </section>
  )
}

function SummaryIconServer() {
  return (
    <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="6" rx="1.5" />
      <rect x="3" y="14" width="18" height="6" rx="1.5" />
      <circle cx="7" cy="7" r="0.6" fill="currentColor" />
      <circle cx="7" cy="17" r="0.6" fill="currentColor" />
    </svg>
  )
}

function SummaryIconChip() {
  return (
    <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <rect x="6" y="6" width="12" height="12" rx="1.5" />
      <rect x="9.5" y="9.5" width="5" height="5" rx="0.5" />
      <path d="M9 3v3M15 3v3M9 18v3M15 18v3M3 9h3M3 15h3M18 9h3M18 15h3" />
    </svg>
  )
}

function SummaryIconStorage() {
  return (
    <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <ellipse cx="12" cy="5" rx="8" ry="2.5" />
      <path d="M4 5v6c0 1.4 3.6 2.5 8 2.5s8-1.1 8-2.5V5" />
      <path d="M4 11v6c0 1.4 3.6 2.5 8 2.5s8-1.1 8-2.5v-6" />
    </svg>
  )
}

function SummaryIconCalendar() {
  return (
    <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <path d="M3 10h18M8 3v4M16 3v4" />
    </svg>
  )
}

function RecentResultsTable({ rows }: { rows: RecentDashboardResult[] }) {
  return (
    <section className="flex h-[320px] flex-col rounded border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex shrink-0 items-center justify-between">
        <h2 className="text-base font-semibold text-slate-900">최근 탐지 결과</h2>
        <Link to="/results" className="text-xs font-medium text-[#109498]">전체 보기</Link>
      </div>
      <div className="mt-4 min-h-0 flex-1 overflow-auto">
        {rows.length === 0 ? <EmptyState /> : (
          <table className="min-w-full text-left text-sm">
            <thead className="text-xs text-slate-500">
              <tr className="border-b border-slate-100">
                <th className="py-3 font-medium">시간</th>
                <th className="py-3 font-medium">설비명</th>
                <th className="py-3 font-medium">검사 유형</th>
                <th className="py-3 font-medium">결과</th>
                <th className="py-3 font-medium">이상 확률</th>
                <th className="py-3 font-medium">위치</th>
                <th className="py-3 font-medium">상세 보기</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.resultId} className="border-b border-slate-50 last:border-0">
                  <td className="whitespace-nowrap py-3 text-slate-600">{formatDateTime(row.inspectedAt)}</td>
                  <td className="py-3 font-medium text-slate-900">{row.equipmentName}</td>
                  <td className="py-3 text-slate-600">{row.inspectionType}</td>
                  <td className="py-3"><DecisionBadge label={row.decisionLabel ?? mapDecision(row.decision)} decision={row.decision} /></td>
                  <td className="py-3"><ScoreBar value={row.anomalyScore ?? 0} /></td>
                  <td className="py-3 text-slate-600">{row.locationName ?? '-'}</td>
                  <td className="py-3">
                    <Link to={`/results/${row.resultId}`} className="rounded border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50">
                      보기
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  )
}

function Bar({ value, max, className }: { value: number; max: number; className: string }) {
  return <div className={`w-4 rounded-t ${className}`} style={{ height: `${Math.max(5, (value / max) * 100)}%` }} />
}

function Legend({ color, label }: { color: string; label: string }) {
  return <span className="flex items-center gap-1.5"><span className={`h-2 w-2 rounded-full ${color}`} />{label}</span>
}

function ScoreBar({ value }: { value: number }) {
  const percent = Math.max(0, Math.min(100, value * 100))
  return <div className="min-w-[120px]"><p className="mb-1 text-xs font-medium text-slate-700">{percent.toFixed(1)}%</p><div className="h-1.5 rounded bg-slate-100"><div className="h-1.5 rounded bg-rose-500" style={{ width: `${percent}%` }} /></div></div>
}

function DecisionBadge({ label, decision }: { label: string; decision: string }) {
  const tone = decision === 'DEFECT' ? 'bg-rose-50 text-rose-700' : decision === 'RECHECK' ? 'bg-amber-50 text-amber-700' : 'bg-emerald-50 text-emerald-700'
  return <span className={`rounded px-2 py-1 text-xs font-semibold ${tone}`}>{label}</span>
}

function EmptyState() {
  return <p className="py-8 text-center text-sm text-slate-500">표시할 데이터가 없습니다.</p>
}

function iconTone(tone: string) {
  const tones: Record<string, string> = {
    blue: 'bg-sky-50',
    red: 'bg-rose-100',
    orange: 'bg-amber-50',
    green: 'bg-emerald-50',
    amber: 'bg-violet-50',
  }
  return tones[tone] ?? 'bg-slate-100'
}

function iconColor(tone: string) {
  const tones: Record<string, string> = {
    blue: 'text-sky-500',
    red: 'text-rose-500',
    orange: 'text-amber-500',
    green: 'text-emerald-500',
    amber: 'text-violet-500',
  }
  return tones[tone] ?? 'text-slate-500'
}

function severityColor(value: string) {
  if (value === 'CRITICAL' || value === 'ERROR') return 'bg-rose-500'
  if (value === 'WARNING') return 'bg-amber-400'
  return 'bg-sky-400'
}

function mapDecision(value: string) {
  if (value === 'DEFECT') return '이상'
  if (value === 'RECHECK') return '재검사'
  if (value === 'NORMAL') return '정상'
  return value
}

function formatBytes(value: number) {
  if (!value) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = value
  let unit = 0
  while (size >= 1024 && unit < units.length - 1) {
    size /= 1024
    unit += 1
  }
  return `${size.toFixed(unit === 0 ? 0 : 2)} ${units[unit]}`
}
