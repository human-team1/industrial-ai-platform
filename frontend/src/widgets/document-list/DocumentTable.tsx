import { StatusBadge } from '../../entities/document/ui/StatusBadge'
import { formatDateTime } from '../../shared/lib/date'
import { IndexingFailureHint } from './IndexingFailureHint'
import type { DocumentTableProps } from './types'

export function DocumentTable({
  items,
  loading,
  error,
  onView,
  onEdit,
  onDelete,
  onRegister,
  onRetry,
  onShowFailureReason,
}: DocumentTableProps) {
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
                      <IndexingFailureHint
                        documentId={item.documentId}
                        onClick={onShowFailureReason}
                      />
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
