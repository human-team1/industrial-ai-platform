import { Link } from 'react-router-dom'
import type {
  DashboardOverview,
  DashboardTrendPoint,
  RecentDashboardNotification,
  RecentDashboardResult,
  TopEquipmentAnomalyRate,
} from '../../entities/dashboard/model/types'
import {
  toDashboardDecisionChart,
  toDashboardEquipmentChart,
  toDashboardTrendChart,
} from '../../features/dashboard/model/chartViewModels'
import { formatDate, formatDateTime } from '../../shared/lib/date'
import { BarChartCard } from '../../shared/ui/chart/BarChartCard'
import { LineChartCard } from '../../shared/ui/chart/LineChartCard'

type Props = {
  data: DashboardOverview
}

export function DashboardOverviewView({ data }: Props) {
  return (
    <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1fr)_320px]">
      <div className="space-y-5">
        <KpiGrid data={data} />
        <div className="grid grid-cols-1 gap-5 lg:grid-cols-[minmax(0,1.45fr)_minmax(280px,0.75fr)]">
          <LineChartCard viewModel={toDashboardTrendChart(data)} empty={{ reason: 'NO_DATA', message: '표시할 데이터가 없습니다.' }} />
          <BarChartCard viewModel={toDashboardEquipmentChart(data)} empty={{ reason: 'NO_DATA', message: '표시할 데이터가 없습니다.' }} />
        </div>
        <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1fr)_320px]">
          <RecentResultsTable rows={data.recentResults} />
          <BarChartCard viewModel={toDashboardDecisionChart(data)} empty={{ reason: 'NO_DATA', message: '표시할 데이터가 없습니다.' }} />
        </div>
      </div>

      <aside className="space-y-5">
        <RecentNotifications rows={data.recentNotifications} />
        <SummaryPanel data={data} />
        <SystemStatusPanel data={data.systemStatus} />
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
    },
    {
      label: '이상 탐지 건수',
      value: `${kpis.anomalyCount.toLocaleString()} 건`,
      change: kpis.anomalyChangeRate,
      tone: 'red',
    },
    {
      label: '정상 건수',
      value: `${kpis.normalCount.toLocaleString()} 건`,
      change: kpis.normalChangeRate,
      tone: 'green',
    },
    {
      label: '이상 탐지율',
      value: `${kpis.anomalyRate.toFixed(2)} %`,
      change: kpis.anomalyRateChangePoint,
      tone: 'amber',
      changeSuffix: 'p',
    },
  ]

  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
      {cards.map((card) => (
        <div key={card.label} className="rounded border border-slate-200 bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between">
            <p className="text-sm font-medium text-slate-500">{card.label}</p>
            <span className={`h-9 w-9 rounded ${iconTone(card.tone)}`} />
          </div>
          <p className="mt-4 text-2xl font-bold text-slate-900">{card.value}</p>
          <p className={`mt-2 text-xs font-medium ${card.change >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
            {card.change >= 0 ? '+' : ''}
            {card.change.toFixed(2)}
            {card.changeSuffix ?? '%'} 지난 기간 대비
          </p>
        </div>
      ))}
    </div>
  )
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
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between">
        <h2 className="text-base font-semibold text-slate-900">실시간 알림</h2>
        <Link to="/notifications" className="text-xs font-medium text-[#109498]">전체 보기</Link>
      </div>
      <div className="mt-4 space-y-3">
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
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="text-base font-semibold text-slate-900">요약 정보</h2>
      <div className="mt-4 grid grid-cols-2 gap-3">
        <SummaryItem label="등록 설비 수" value={`${summary.registeredTargetCount.toLocaleString()}개`} />
        <SummaryItem label="활성 모델 수" value={`${summary.activeModelCount.toLocaleString()}개`} />
        <SummaryItem label="총 데이터 용량" value={formatBytes(summary.totalDataSizeBytes)} />
        <SummaryItem label="최근 학습 일자" value={formatDate(summary.latestTrainingDate ?? undefined)} />
      </div>
    </section>
  )
}

function SystemStatusPanel({ data }: { data: DashboardOverview['systemStatus'] }) {
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="text-base font-semibold text-slate-900">시스템 상태</h2>
      <div className="mt-4 space-y-3 text-sm">
        <StatusRow label="전체" value={data.overallStatus} />
        <StatusRow label="모델 서버" value={data.modelServerStatus} />
        <StatusRow label="스트리밍 서버" value={data.streamServerStatus} />
        <StatusRow label="스토리지" value={data.storageStatus} />
        <p className="pt-1 text-xs text-slate-500">최근 업데이트 {formatDateTime(data.lastUpdatedAt)}</p>
      </div>
    </section>
  )
}

function RecentResultsTable({ rows }: { rows: RecentDashboardResult[] }) {
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between">
        <h2 className="text-base font-semibold text-slate-900">최근 탐지 결과</h2>
        <Link to="/results" className="text-xs font-medium text-[#109498]">전체 보기</Link>
      </div>
      <div className="mt-4 overflow-x-auto">
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

function SummaryItem({ label, value }: { label: string; value: string }) {
  return <div className="rounded bg-slate-50 p-3"><p className="text-xs text-slate-500">{label}</p><p className="mt-1 text-sm font-semibold text-slate-900">{value}</p></div>
}

function StatusRow({ label, value }: { label: string; value: string }) {
  return <div className="flex items-center justify-between"><span className="text-slate-500">{label}</span><span className={`rounded px-2 py-1 text-xs font-semibold ${statusTone(value)}`}>{statusLabel(value)}</span></div>
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
    blue: 'bg-blue-100',
    red: 'bg-rose-100',
    green: 'bg-emerald-100',
    amber: 'bg-amber-100',
  }
  return tones[tone] ?? 'bg-slate-100'
}

function severityColor(value: string) {
  if (value === 'CRITICAL' || value === 'ERROR') return 'bg-rose-500'
  if (value === 'WARNING') return 'bg-amber-400'
  return 'bg-sky-400'
}

function statusTone(value: string) {
  if (value === 'ERROR') return 'bg-rose-50 text-rose-700'
  if (value === 'WARNING') return 'bg-amber-50 text-amber-700'
  return 'bg-emerald-50 text-emerald-700'
}

function statusLabel(value: string) {
  if (value === 'ERROR') return '오류'
  if (value === 'WARNING') return '주의'
  return '정상'
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
