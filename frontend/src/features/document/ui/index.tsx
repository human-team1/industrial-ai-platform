import type { DocumentListItem, DocumentSummary, IndexingStatus } from '../types'

export function indexingStatusLabel(status: IndexingStatus | string | undefined | null): string {
  if (!status) return '-'
  const value = String(status).toUpperCase()
  if (value === 'PENDING') return '대기'
  if (value === 'PROCESSING') return '처리중'
  if (value === 'COMPLETED') return '반영완료'
  if (value === 'FAILED') return '반영실패'
  return String(status)
}

export function DocumentSummaryCards({ summary }: { summary: DocumentSummary | null }) {
  const data = summary ?? {
    totalCount: 0,
    pdfCount: 0,
    docxCount: 0,
    completedCount: 0,
    processingCount: 0,
    failedCount: 0,
  }

  return (
    <section className="grid grid-cols-1 gap-4 md:grid-cols-3 xl:grid-cols-6">
      <Card label="전체" value={data.totalCount} />
      <Card label="PDF" value={data.pdfCount} />
      <Card label="DOCX" value={data.docxCount} />
      <Card label="반영완료" value={data.completedCount} />
      <Card label="처리중" value={data.processingCount} />
      <Card label="반영실패" value={data.failedCount} />
    </section>
  )
}

export function DocumentTable({
  items,
  loading,
  error,
  onEdit,
  onDelete,
}: {
  items: DocumentListItem[]
  loading: boolean
  error: string | null
  onEdit: (id: number) => void
  onDelete: (id: number) => void
}) {
  if (loading) return <div className="page-panel">문서 목록을 불러오는 중입니다.</div>
  if (error) return <div className="page-panel text-red-600">{error}</div>
  if (items.length === 0) return <div className="page-panel">조회된 문서가 없습니다.</div>

  return (
    <section className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="w-full min-w-[980px] border-collapse text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              {['문서명', '유형', '카테고리', '태그', '업로드일', '인덱싱 상태', '관리'].map((head) => (
                <th key={head} className="border-b border-slate-200 px-4 py-3 font-semibold">
                  {head}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.documentId} className="border-b border-slate-100 last:border-0">
                <td className="px-4 py-3 font-medium text-slate-900">{item.title}</td>
                <td className="px-4 py-3">{item.documentType}</td>
                <td className="px-4 py-3">{item.category || '-'}</td>
                <td className="px-4 py-3">{item.tags?.join(', ') || '-'}</td>
                <td className="px-4 py-3">{formatDateTime(item.createdAt)}</td>
                <td className="px-4 py-3">
                  <StatusBadge status={item.indexingStatus} />
                </td>
                <td className="space-x-2 px-4 py-3">
                  <button className="btn-secondary" onClick={() => onEdit(item.documentId)}>
                    수정
                  </button>
                  <button className="btn-secondary" onClick={() => onDelete(item.documentId)}>
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

function formatDateTime(value?: string | null) {
  if (!value) return '-'
  return value.slice(0, 16).replace('T', ' ')
}
