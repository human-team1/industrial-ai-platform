import { Link } from 'react-router-dom'
import { formatDateTime } from '../../../shared/lib/date'
import { indexingStatusLabel } from '../lib/indexingStatusLabel'
import type { DocumentListItem, DocumentSummary, IndexingStatus } from '../types'

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
            <div className="mb-2 h-4 w-16 rounded bg-slate-100" />
            <div className="h-7 w-24 rounded bg-slate-100" />
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

  return (
    <section className="grid grid-cols-1 gap-4 md:grid-cols-3 xl:grid-cols-6">
      <Card label="전체" value={summary.totalCount} />
      <Card label="PDF" value={summary.pdfCount} />
      <Card label="DOCX" value={summary.docxCount} />
      <Card label="반영완료" value={summary.completedCount} />
      <Card label="처리중" value={summary.processingCount} />
      <Card label="반영실패" value={summary.failedCount} />
    </section>
  )
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

function Card({ label, value }: { label: string; value: number }) {
  return (
    <article className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="text-xl font-semibold text-slate-900">{value.toLocaleString()}</p>
    </article>
  )
}
