import { useEffect, useState, type ReactNode } from 'react'
import { apiClient } from '../../../shared/api/client'
import {
  formatDateMinute,
  labelDecision,
  labelRunType,
  mapResultListRow,
} from '../model/mapper'
import type {
  AnomalyRegion,
  RelatedResult,
  ResultArtifact,
  ResultChecklistItem,
  ResultDecisionInfo,
  ResultDescription,
  ResultEventLog,
  ResultImage,
  ResultInspection,
  ResultListQuery,
  ResultListSummary,
  ResultPageResponse,
  ResultSummary,
  ResultTarget,
} from '../types'

const eventLabels: Record<string, string> = {
  INSPECTION_STARTED: '탐지 시작',
  IMAGE_CAPTURED: '이미지 수집',
  ANOMALY_DETECTED: '이상 탐지',
  INSPECTION_FAILED: '탐지 실패',
}

const priorityLabels: Record<string, string> = {
  REQUIRED: '필수',
  RECOMMENDED: '권장',
  OPTIONAL: '선택',
}

export function ResultListFilter({
  filters,
  loading,
  onChange,
  onSearch,
  onReset,
}: {
  filters: ResultListQuery
  loading: boolean
  onChange: (filters: ResultListQuery) => void
  onSearch: () => void
  onReset: () => void
}) {
  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="grid grid-cols-1 gap-3 lg:grid-cols-[2fr_1.4fr_1fr_1fr_1fr_auto]">
        <Field label="키워드">
          <input
            value={filters.keyword ?? ''}
            onChange={(event) => onChange({ ...filters, keyword: event.target.value })}
            placeholder="설비명, 위치, 검사 유형 검색"
            className="control"
          />
        </Field>

        <Field label="날짜 범위">
          <div className="grid grid-cols-2 gap-2">
            <input type="datetime-local" value={filters.from ?? ''} onChange={(event) => onChange({ ...filters, from: event.target.value })} className="control" />
            <input type="datetime-local" value={filters.to ?? ''} onChange={(event) => onChange({ ...filters, to: event.target.value })} className="control" />
          </div>
        </Field>

        <Field label="설비명">
          <input
            value={filters.equipmentName ?? ''}
            onChange={(event) => onChange({ ...filters, equipmentName: event.target.value })}
            placeholder="설비명 (부분 일치)"
            className="control"
          />
        </Field>

        <Field label="검사 유형">
          <select value={filters.runType ?? ''} onChange={(event) => onChange({ ...filters, runType: event.target.value })} className="control">
            <option value="">전체 검사 유형</option>
            <option value="REALTIME">실시간 탐지</option>
            <option value="UPLOAD">업로드 탐지</option>
          </select>
        </Field>

        <Field label="결과">
          <select value={filters.decision ?? ''} onChange={(event) => onChange({ ...filters, decision: event.target.value as ResultListQuery['decision'] })} className="control">
            <option value="">전체 결과</option>
            <option value="NORMAL">정상</option>
            <option value="DEFECT">이상</option>
            <option value="RETEST">재검사</option>
          </select>
        </Field>

        <div className="flex items-end gap-2">
          <button type="button" onClick={onReset} disabled={loading} className="btn-secondary whitespace-nowrap">필터 초기화</button>
          <button type="button" onClick={onSearch} disabled={loading} className="btn-primary whitespace-nowrap">검색</button>
        </div>
      </div>
    </section>
  )
}

export function ResultSummaryCards({
  summary,
  loading,
}: {
  summary: ResultListSummary | null
  loading?: boolean
}) {
  if (loading) {
    return (
      <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        {[1, 2, 3, 4].map((key) => (
          <article key={key} className="animate-pulse rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
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
      <SummaryCard label="정상" value={`${Number(summary.normalCount).toLocaleString()}건`} sub={ratio(Number(summary.normalCount), total)} tone="green" />
      <SummaryCard label="이상" value={`${Number(summary.defectCount).toLocaleString()}건`} sub={ratio(Number(summary.defectCount), total)} tone="red" />
      <SummaryCard label="평균 이상 점수" value={avg} tone="orange" />
    </section>
  )
}

export function AppliedFilterChips({ filters, onReset }: { filters: ResultListQuery; onReset: () => void }) {
  const chips = [
    filters.from || filters.to ? `기간: ${formatShortDate(filters.from)}~${formatShortDate(filters.to)}` : null,
    filters.equipmentName ? `설비: ${filters.equipmentName}` : '전체 설비',
    filters.runType ? `검사 유형: ${labelRunType(filters.runType)}` : '전체 검사 유형',
    filters.decision ? `결과: ${labelDecision(filters.decision)}` : '전체 결과',
    filters.keyword ? `검색: ${filters.keyword}` : null,
  ].filter(Boolean)

  return (
    <section className="flex flex-wrap items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm">
      <span className="text-sm font-semibold text-slate-700">적용 필터</span>
      {chips.map((chip) => (
        <span key={chip} className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700">{chip}</span>
      ))}
      <button type="button" onClick={onReset} className="ml-auto text-xs font-semibold text-slate-500 hover:text-slate-900">전체 초기화</button>
    </section>
  )
}

export function ResultListTable({
  data,
  loading,
  error,
  empty,
  onRetry,
  onResetFilters,
  onDetail,
}: {
  data: ResultPageResponse | null
  loading: boolean
  error: string | null
  empty: boolean
  onRetry: () => void
  onResetFilters: () => void
  onDetail: (resultId: number) => void
}) {
  if (loading) return <StateBox title="검사 결과를 불러오는 중입니다." />
  if (error) return <StateBox title={error} actionLabel="재시도" onAction={onRetry} />
  if (empty) {
    return (
      <StateBox
        title="조회된 검사 결과가 없습니다."
        actionLabel="필터 초기화"
        onAction={onResetFilters}
      />
    )
  }

  return (
    <section className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-[1100px] w-full border-collapse text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              {['결과 ID', '검사 ID', '검사시간', '설비/대상', '검사유형', '판정', '이상점수', '상태', '상세'].map((head) => (
                <th key={head} className="border-b border-slate-200 px-4 py-3 font-semibold">{head}</th>
              ))}
            </tr>
          </thead>
          <tbody>{data?.content.map((item) => <ResultRow key={item.resultId} item={item} onDetail={onDetail} />)}</tbody>
        </table>
      </div>
    </section>
  )
}

export function OriginalImageCard({ images }: { images: ResultImage[] }) {
  const original = images.find((image) => image.imageRole === 'ORIGINAL') ?? images[0]
  return (
    <InfoCard title="원본 이미지">
      <PreviewImage fileId={original?.fileId} emptyText="원본 이미지가 없습니다." />
    </InfoCard>
  )
}

export function VisualizationCard({ artifacts, regions }: { artifacts: ResultArtifact[]; regions: AnomalyRegion[] }) {
  const artifact = artifacts.find((item) => ['HEATMAP', 'VISUALIZATION', 'ANOMALY_MAP'].includes(String(item.artifactType))) ?? artifacts[0]
  return (
    <InfoCard title="이상 탐지 시각화">
      <PreviewImage fileId={artifact?.fileId} emptyText="시각화 이미지가 없습니다." />
      {regions.length > 0 ? (
        <div className="mt-4 space-y-2">
          {regions.map((region) => (
            <div key={region.regionId} className="flex items-center justify-between rounded border border-slate-200 px-3 py-2 text-sm">
              <span>{display(region.labelCode)}</span>
              <span className="text-slate-500">score {formatPercent(region.score)}</span>
            </div>
          ))}
        </div>
      ) : null}
    </InfoCard>
  )
}

export function EventLogCard({ eventLogs }: { eventLogs?: ResultEventLog[] | null }) {
  const logs = eventLogs ?? []
  return (
    <InfoCard title="이벤트 로그">
      {logs.length === 0 ? <p className="text-sm text-slate-500">이벤트 로그가 없습니다.</p> : (
        <ol className="space-y-3">
          {logs.map((log) => (
            <li key={log.eventId} className="flex gap-3 text-sm">
              <span className="mt-1 h-2.5 w-2.5 rounded-full bg-slate-400" />
              <div>
                <p className="font-medium text-slate-900">{eventLabels[String(log.eventType)] ?? display(log.message)}</p>
                <p className="text-slate-500">{formatDateMinute(log.createdAt)}</p>
              </div>
            </li>
          ))}
        </ol>
      )}
    </InfoCard>
  )
}

export function DetectionInfoCard({ target, inspection, result, originalImage }: {
  target: ResultTarget
  inspection: ResultInspection
  result: ResultDecisionInfo
  originalImage?: ResultImage
}) {
  return (
    <InfoCard title="탐지 결과 정보">
      <InfoGrid items={[
        ['설비명', target.equipmentName],
        ['검사유형', labelRunType(inspection.runType)],
        ['탐지일시', formatDateMinute(inspection.startedAt ?? result.createdAt)],
        ['위치', target.targetName],
        ['모델버전', result.modelVersionId],
        ['이미지ID / 파일ID', originalImage ? `${originalImage.imageId} / ${display(originalImage.fileId)}` : '-'],
      ]} />
    </InfoCard>
  )
}

export function ProbabilityDecisionCard({ result }: { result: ResultDecisionInfo }) {
  const decision = result.finalDecisionCode ?? result.decisionCode
  const percent = toPercent(result.score)
  return (
    <InfoCard title="이상 확률 / 판정">
      <div className="flex items-end justify-between gap-4">
        <div>
          <p className="text-4xl font-bold text-slate-950">{percent}</p>
          <p className="mt-1 text-sm text-slate-500">anomaly score</p>
        </div>
        <DecisionBadge value={decision} />
      </div>
      <div className="mt-5 h-3 overflow-hidden rounded-full bg-slate-100">
        <div className={`h-full ${progressColor(decision)}`} style={{ width: percent === '-' ? '0%' : percent }} />
      </div>
    </InfoCard>
  )
}

export function DescriptionCard({ description }: { description?: ResultDescription | null }) {
  return (
    <InfoCard title="이상 설명">
      {description?.summary || description?.recommendedAction ? (
        <div className="space-y-3 text-sm leading-6 text-slate-700">
          <p>{description.summary}</p>
          <p className="rounded bg-slate-50 p-3 font-medium text-slate-900">{description.recommendedAction}</p>
        </div>
      ) : <p className="text-sm text-slate-500">이상 설명이 없습니다.</p>}
    </InfoCard>
  )
}

export function ChecklistCard({ checklist }: { checklist?: ResultChecklistItem[] | null }) {
  const items = checklist ?? []
  return (
    <InfoCard title="대응 절차 및 체크리스트">
      {items.length === 0 ? <p className="text-sm text-slate-500">표시할 대응 절차가 없습니다.</p> : (
        <div className="space-y-3">
          {items.map((item) => (
            <div key={`${item.title}-${item.priority}`} className="rounded border border-slate-200 p-3">
              <div className="flex items-center justify-between gap-2">
                <p className="font-medium text-slate-900">{item.title}</p>
                <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-600">{priorityLabels[item.priority] ?? item.priority}</span>
              </div>
              <p className="mt-1 text-sm text-slate-600">{display(item.description)}</p>
            </div>
          ))}
        </div>
      )}
    </InfoCard>
  )
}

export function RelatedResultsCard({ relatedResults, onDetail }: {
  relatedResults?: RelatedResult[] | null
  onDetail: (resultId: number) => void
}) {
  const items = relatedResults ?? []
  return (
    <InfoCard title="관련 탐지 이력">
      {items.length === 0 ? <p className="text-sm text-slate-500">관련 탐지 이력이 없습니다.</p> : (
        <div className="space-y-2">
          {items.map((item) => (
            <div key={item.resultId} className="grid grid-cols-[1fr_auto] gap-3 rounded border border-slate-200 p-3 text-sm">
              <div>
                <p className="font-medium text-slate-900">{formatDateMinute(item.createdAt)}</p>
                <p className="mt-1 text-slate-500">{display(item.location)} · {formatPercent(item.score)} · {labelDecision(item.decisionCode)}</p>
              </div>
              <button type="button" onClick={() => onDetail(item.resultId)} className="btn-secondary">상세 보기</button>
            </div>
          ))}
        </div>
      )}
    </InfoCard>
  )
}

function ResultRow({ item, onDetail }: { item: ResultSummary; onDetail: (resultId: number) => void }) {
  const mapped = mapResultListRow(item)

  return (
    <tr
      className="cursor-pointer border-b border-slate-100 last:border-0 hover:bg-slate-50"
      onClick={() => onDetail(item.resultId)}
    >
      <td className="px-4 py-3 font-mono text-xs text-slate-700">{mapped.resultId}</td>
      <td className="px-4 py-3 font-mono text-xs text-slate-700">{mapped.inspectionId}</td>
      <td className="px-4 py-3">{mapped.inspectedAt}</td>
      <td className="px-4 py-3">{display(mapped.targetLine)}</td>
      <td className="px-4 py-3">{mapped.runTypeLabel}</td>
      <td className="px-4 py-3"><DecisionBadge value={mapped.decision} /></td>
      <td className="px-4 py-3"><ScoreBar value={mapped.score} decision={mapped.decision} /></td>
      <td className="px-4 py-3 text-xs text-slate-600">{mapped.statusLabel}</td>
      <td className="px-4 py-3 text-right">
        <button
          type="button"
          onClick={(event) => {
            event.stopPropagation()
            onDetail(item.resultId)
          }}
          className="btn-secondary"
        >
          상세
        </button>
      </td>
    </tr>
  )
}

function SummaryCard({ label, value, sub, tone }: { label: string; value: string; sub?: string; tone: 'slate' | 'green' | 'red' | 'orange' }) {
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

function PreviewImage({ fileId, emptyText }: { fileId?: number | null; emptyText: string }) {
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    if (!fileId) {
      setPreviewUrl(null)
      setFailed(false)
      return
    }

    let objectUrl: string | null = null
    setFailed(false)

    apiClient.get(`/files/${fileId}/preview`, { responseType: 'blob' })
      .then((response) => {
        objectUrl = URL.createObjectURL(response.data)
        setPreviewUrl(objectUrl)
      })
      .catch(() => {
        setFailed(true)
        setPreviewUrl(null)
      })

    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [fileId])

  if (!fileId) return <ImageEmpty text={emptyText} />
  if (failed) return <ImageEmpty text="이미지가 없습니다." />
  if (!previewUrl) return <ImageEmpty text="이미지를 불러오는 중입니다." />
  return <img src={previewUrl} alt="" className="h-72 w-full rounded-lg bg-slate-100 object-contain" />
}

function ImageEmpty({ text }: { text: string }) {
  return <div className="flex h-72 items-center justify-center rounded-lg bg-slate-100 text-sm text-slate-500">{text}</div>
}

function DecisionBadge({ value }: { value?: string | null }) {
  const normalized = String(value ?? '')
  const className = normalized === 'NORMAL'
    ? 'bg-emerald-50 text-emerald-700 ring-emerald-200'
    : ['RETEST', 'RECHECK', 'REINSPECTION'].includes(normalized)
      ? 'bg-orange-50 text-orange-700 ring-orange-200'
      : 'bg-red-50 text-red-700 ring-red-200'
  return <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${className}`}>{labelDecision(normalized)}</span>
}

function ScoreBar({ value, decision }: { value?: number | null; decision?: string | null }) {
  const percent = toPercent(value)
  return (
    <div className="min-w-32">
      <div className="mb-1 text-xs font-semibold text-slate-700">{formatScore(value)}</div>
      <div className="h-2 overflow-hidden rounded-full bg-slate-100">
        <div className={`h-full ${progressColor(decision)}`} style={{ width: percent === '-' ? '0%' : percent }} />
      </div>
    </div>
  )
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">{label}{children}</label>
}

function InfoCard({ title, children }: { title: string; children: ReactNode }) {
  return <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm"><h2 className="mb-4 text-base font-semibold text-slate-950">{title}</h2>{children}</section>
}

function InfoGrid({ items }: { items: Array<[string, ReactNode]> }) {
  return <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">{items.map(([label, value]) => <div key={label}><dt className="text-xs font-semibold uppercase text-slate-500">{label}</dt><dd className="mt-1 text-sm text-slate-900">{display(value)}</dd></div>)}</dl>
}

function StateBox({ title, actionLabel, onAction }: { title: string; actionLabel?: string; onAction?: () => void }) {
  return (
    <div className="flex min-h-40 flex-col items-center justify-center gap-3 rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-600 shadow-sm">
      <p>{title}</p>
      {actionLabel && onAction ? (
        <button type="button" onClick={onAction} className="btn-primary">
          {actionLabel}
        </button>
      ) : null}
    </div>
  )
}

function display(value: ReactNode) {
  return value === null || value === undefined || value === '' ? '-' : value
}

function toPercent(value?: number | null) {
  if (value === null || value === undefined) return '-'
  return `${Math.max(0, Math.min(100, Number(value) * 100)).toFixed(1)}%`
}

function formatPercent(value?: number | null) {
  return toPercent(value)
}

function formatScore(value?: number | null) {
  if (value === null || value === undefined) return '-'
  return Number(value).toFixed(2)
}

function formatShortDate(value?: string | null) {
  if (!value) return ''
  return value.slice(0, 10)
}

function progressColor(value?: string | null) {
  if (value === 'NORMAL') return 'bg-emerald-500'
  if (['RETEST', 'RECHECK', 'REINSPECTION'].includes(String(value))) return 'bg-orange-500'
  return 'bg-red-500'
}

function ratio(count: number, total: number) {
  if (!total) return '0.0%'
  return `${((count / total) * 100).toFixed(1)}%`
}
