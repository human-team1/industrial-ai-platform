import { useEffect, useState, type ReactNode } from 'react'
import { apiClient } from '../../../shared/api/client'
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
  ResultPageResponse,
  ResultSummary,
  ResultTarget,
  ReviewQueueSummary,
} from '../types'

const decisionLabels: Record<string, string> = {
  NORMAL: '정상',
  DEFECT: '이상',
  RETEST: '재검사',
  RECHECK: '재검사',
  REINSPECTION: '재검사',
}

const statusLabels: Record<string, string> = {
  SUCCESS: '성공',
  FAILED: '실패',
  REVIEW_REQUIRED: '재검토 대상',
  CORRECTED: '수정 완료',
}

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
      <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-6">
        <Field label="시작일시">
          <input type="datetime-local" value={filters.from ?? ''} onChange={(event) => onChange({ ...filters, from: event.target.value })} className="control" />
        </Field>
        <Field label="종료일시">
          <input type="datetime-local" value={filters.to ?? ''} onChange={(event) => onChange({ ...filters, to: event.target.value })} className="control" />
        </Field>
        <Field label="설비명">
          <input value={filters.equipmentName ?? ''} onChange={(event) => onChange({ ...filters, equipmentName: event.target.value })} className="control" />
        </Field>
        <Field label="품목명">
          <input value={filters.productName ?? ''} onChange={(event) => onChange({ ...filters, productName: event.target.value })} className="control" />
        </Field>
        <Field label="판정">
          <select value={filters.decision ?? ''} onChange={(event) => onChange({ ...filters, decision: event.target.value as ResultListQuery['decision'] })} className="control">
            <option value="">전체</option>
            <option value="NORMAL">정상</option>
            <option value="DEFECT">이상</option>
            <option value="RETEST">재검사</option>
          </select>
        </Field>
        <Field label="상태">
          <select value={filters.resultStatus ?? ''} onChange={(event) => onChange({ ...filters, resultStatus: event.target.value as ResultListQuery['resultStatus'] })} className="control">
            <option value="">전체</option>
            <option value="SUCCESS">성공</option>
            <option value="FAILED">실패</option>
            <option value="REVIEW_REQUIRED">재검토 대상</option>
            <option value="CORRECTED">수정 완료</option>
          </select>
        </Field>
      </div>
      <div className="mt-4 flex justify-end gap-2">
        <button type="button" onClick={onReset} disabled={loading} className="btn-secondary">초기화</button>
        <button type="button" onClick={onSearch} disabled={loading} className="btn-primary">검색</button>
      </div>
    </section>
  )
}

export function ResultListTable({
  data,
  loading,
  error,
  empty,
  onRetry,
  onDetail,
}: {
  data: ResultPageResponse | null
  loading: boolean
  error: string | null
  empty: boolean
  onRetry: () => void
  onDetail: (resultId: number) => void
}) {
  if (loading) return <StateBox title="검사 결과를 불러오는 중입니다." />
  if (error) return <StateBox title={error} actionLabel="재시도" onAction={onRetry} />
  if (empty) return <StateBox title="조회된 검사 결과가 없습니다." />

  return (
    <section className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-[1120px] w-full border-collapse text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              {['검사 ID', '결과 ID', '검사 대상', '설비명', '품목명', '입력 유형', '이상 확률', '판정', '상태', '검사 시작일시', ''].map((head) => (
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

export function ResultPagination({ page, totalPages, totalElements, onPageChange }: {
  page: number
  totalPages: number
  totalElements: number
  onPageChange: (page: number) => void
}) {
  return (
    <div className="flex flex-col gap-3 text-sm text-slate-600 sm:flex-row sm:items-center sm:justify-between">
      <span>총 {totalElements.toLocaleString()}건</span>
      <div className="flex items-center gap-2">
        <button type="button" onClick={() => onPageChange(Math.max(0, page - 1))} disabled={page <= 0} className="btn-secondary">이전</button>
        <span>{totalPages === 0 ? 0 : page + 1} / {totalPages}</span>
        <button type="button" onClick={() => onPageChange(page + 1)} disabled={page + 1 >= totalPages} className="btn-secondary">다음</button>
      </div>
    </div>
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
                <p className="text-slate-500">{formatDateTime(log.createdAt)}</p>
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
        ['검사유형', inspection.runType],
        ['탐지일시', formatDateTime(inspection.startedAt ?? result.createdAt)],
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
                <p className="font-medium text-slate-900">{formatDateTime(item.createdAt)}</p>
                <p className="mt-1 text-slate-500">{display(item.location)} · {formatPercent(item.score)} · {decisionLabels[String(item.decisionCode)] ?? display(item.decisionCode)}</p>
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
  return (
    <tr className="border-b border-slate-100 last:border-0">
      <td className="px-4 py-3">{display(item.inspectionId)}</td>
      <td className="px-4 py-3">{display(item.resultId)}</td>
      <td className="px-4 py-3">{display(item.targetName)}</td>
      <td className="px-4 py-3">{display(item.equipmentName)}</td>
      <td className="px-4 py-3">{display(item.productName)}</td>
      <td className="px-4 py-3">{display(item.inputType)}</td>
      <td className="px-4 py-3"><ScoreBar value={item.score} /></td>
      <td className="px-4 py-3"><DecisionBadge value={item.finalDecisionCode ?? item.decisionCode} /></td>
      <td className="px-4 py-3">{statusLabels[String(item.resultStatus)] ?? display(item.resultStatus)}</td>
      <td className="px-4 py-3">{formatDateTime(item.startedAt)}</td>
      <td className="px-4 py-3 text-right"><button type="button" onClick={() => onDetail(item.resultId)} className="btn-secondary">상세 보기</button></td>
    </tr>
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
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl)
      }
    }
  }, [fileId])

  if (!fileId) {
    return <div className="flex h-72 items-center justify-center rounded-lg bg-slate-100 text-sm text-slate-500">{emptyText}</div>
  }
  if (failed) {
    return <div className="flex h-72 items-center justify-center rounded-lg bg-slate-100 text-sm text-slate-500">이미지가 없습니다.</div>
  }
  if (!previewUrl) {
    return <div className="flex h-72 items-center justify-center rounded-lg bg-slate-100 text-sm text-slate-500">이미지를 불러오는 중입니다.</div>
  }
  return <img src={previewUrl} alt="" className="h-72 w-full rounded-lg bg-slate-100 object-contain" />
}

function DecisionBadge({ value }: { value?: string | null }) {
  const normalized = String(value ?? '')
  const className = normalized === 'NORMAL'
    ? 'bg-emerald-50 text-emerald-700 ring-emerald-200'
    : ['RETEST', 'RECHECK', 'REINSPECTION'].includes(normalized)
      ? 'bg-orange-50 text-orange-700 ring-orange-200'
      : 'bg-red-50 text-red-700 ring-red-200'
  return <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${className}`}>{decisionLabels[normalized] ?? display(value)}</span>
}

function ScoreBar({ value }: { value?: number | null }) {
  const percent = toPercent(value)
  return (
    <div className="min-w-32">
      <div className="mb-1 text-xs font-semibold text-slate-700">{percent}</div>
      <div className="h-2 overflow-hidden rounded-full bg-slate-100">
        <div className="h-full bg-red-500" style={{ width: percent === '-' ? '0%' : percent }} />
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
  return <div className="flex min-h-40 flex-col items-center justify-center gap-3 rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-600 shadow-sm"><p>{title}</p>{actionLabel && onAction ? <button type="button" onClick={onAction} className="btn-primary">{actionLabel}</button> : null}</div>
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

function formatDateTime(value?: string | null) {
  if (!value) return '-'
  return new Date(value).toLocaleString('ko-KR')
}

function progressColor(value?: string | null) {
  if (value === 'NORMAL') return 'bg-emerald-500'
  if (['RETEST', 'RECHECK', 'REINSPECTION'].includes(String(value))) return 'bg-orange-500'
  return 'bg-red-500'
}
