import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { formatDateTime } from '../../../shared/lib/date'
import { indexingStatusLabel } from '../lib/indexingStatusLabel'
import type { DocumentListItem, DocumentSummary, IndexingStatus } from '../types'

type SummaryTone = 'blue' | 'rose' | 'indigo' | 'green' | 'amber' | 'red'

export function DocumentSummaryCards({
  summary,
  loading,
}: {
  summary: DocumentSummary | null
  loading?: boolean
}) {
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
          `GET /documents/summary` 응답이 없거나 오류입니다. 카드에 0으로 채우지 않습니다.
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

export function DocumentTable({
  items,
  loading,
  error,
  onView,
  onEdit,
  onDelete,
  onRegister,
  onRetry,
}: {
  items: DocumentListItem[]
  loading: boolean
  error: string | null
  onView: (id: number) => void
  onEdit: (id: number) => void
  onDelete: (id: number) => void
  onRegister: () => void
  onRetry?: () => void
}) {
  if (loading) return <div className="page-panel">문서 목록을 불러오는 중입니다.</div>
  if (error) {
    return (
      <div className="page-panel flex flex-col items-center gap-3 text-center text-red-600">
        <p>{error}</p>
        {onRetry ? (
          <button type="button" className="btn-secondary" onClick={onRetry}>
            재시도
          </button>
        ) : null}
      </div>
    )
  }
  if (items.length === 0) {
    return (
      <div className="page-panel flex flex-col items-center gap-3 text-center text-slate-600">
        <p>조회된 문서가 없습니다.</p>
        <button type="button" className="btn-primary" onClick={onRegister}>
          문서 등록
        </button>
      </div>
    )
  }

  return (
    <section className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="w-full min-w-[1180px] border-collapse text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              {[
                '문서명',
                '유형',
                '카테고리',
                '설비 유형',
                '태그',
                '반영 상태',
                '버전',
                '등록/수정',
                '관리',
              ].map((head) => (
                <th key={head} className="border-b border-slate-200 px-4 py-3 font-semibold">
                  {head}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.documentId} className="border-b border-slate-100 last:border-0 hover:bg-slate-50">
                <td className="px-4 py-3 font-medium text-slate-900">{item.title}</td>
                <td className="px-4 py-3">{item.documentType}</td>
                <td className="px-4 py-3">{item.category || '-'}</td>
                <td className="px-4 py-3">{item.equipmentType || '-'}</td>
                <td className="max-w-[200px] truncate px-4 py-3" title={item.tags?.join(', ')}>
                  {item.tags?.join(', ') || '-'}
                </td>
                <td className="px-4 py-3">
                  <div className="flex flex-col gap-1">
                    <StatusBadge status={item.indexingStatus} />
                    {item.indexingStatus === 'FAILED' && item.documentId ? (
                      <IndexingFailureHint documentId={item.documentId} />
                    ) : null}
                  </div>
                </td>
                <td className="px-4 py-3 font-mono text-xs">v{item.versionNo}</td>
                <td className="px-4 py-3 text-xs text-slate-600">
                  <div>{formatDateTime(item.createdAt ?? undefined)}</div>
                  <div>{formatDateTime(item.updatedAt ?? undefined)}</div>
                </td>
                <td className="space-x-2 whitespace-nowrap px-4 py-3">
                  <button type="button" className="btn-secondary" onClick={() => onView(item.documentId)}>
                    상세
                  </button>
                  <button type="button" className="btn-secondary" onClick={() => onEdit(item.documentId)}>
                    수정
                  </button>
                  <button type="button" className="btn-secondary" onClick={() => onDelete(item.documentId)}>
                    삭제
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}

function IndexingFailureHint({ documentId }: { documentId: number }) {
  return (
    <p className="text-xs text-red-600" title="상세 화면에서 인덱싱 실패 사유를 확인할 수 있습니다.">
      반영 실패 ·{' '}
      <Link className="font-semibold underline" to={`/documents/${documentId}/edit`}>
        사유 확인
      </Link>
    </p>
  )
}

function StatusBadge({ status }: { status: IndexingStatus }) {
  const color =
    status === 'PENDING'
      ? 'bg-slate-100 text-slate-700'
      : status === 'PROCESSING'
        ? 'bg-blue-100 text-blue-700'
        : status === 'COMPLETED'
          ? 'bg-emerald-100 text-emerald-700'
          : 'bg-red-100 text-red-700'

  return <span className={`rounded-full px-2 py-1 text-xs font-semibold ${color}`}>{indexingStatusLabel(status)}</span>
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
          <span
            className={`flex h-12 w-12 items-center justify-center rounded-full ${iconBg(tone)} ${iconColor(tone)}`}
          >
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
