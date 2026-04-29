import { useNavigate } from 'react-router-dom'
import { removeDocument, useDocumentList } from '../features/document/model'
import { DocumentSummaryCards, DocumentTable } from '../features/document/ui'

export function DocumentListPage() {
  const navigate = useNavigate()
  const { query, setQuery, data, summary, loading, error, reload } = useDocumentList()

  const onDelete = async (documentId: number) => {
    if (!window.confirm('문서를 삭제하시겠습니까?')) return
    await removeDocument(documentId)
    await reload()
  }

  return (
    <section className="space-y-5">
      <div className="page-panel flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">문서 관리</h1>
          <p className="mt-2 text-sm text-slate-600">문서 목록 조회, 등록, 수정, 삭제를 관리합니다.</p>
        </div>
        <button className="btn-primary" onClick={() => navigate('/documents/new')}>문서 등록</button>
      </div>

      <DocumentSummaryCards summary={summary} />
      <div className="page-panel grid grid-cols-1 gap-3 md:grid-cols-4">
        <input
          className="control"
          placeholder="제목/설명/태그 검색"
          value={query.keyword ?? ''}
          onChange={(e) => setQuery((prev) => ({ ...prev, keyword: e.target.value, page: 0 }))}
        />
        <select
          className="control"
          value={query.documentType ?? ''}
          onChange={(e) => setQuery((prev) => ({ ...prev, documentType: e.target.value || undefined, page: 0 }))}
        >
          <option value="">전체 유형</option>
          <option value="PDF">PDF</option>
          <option value="DOCX">DOCX</option>
          <option value="TXT">TXT</option>
        </select>
        <input
          className="control"
          placeholder="카테고리"
          value={query.category ?? ''}
          onChange={(e) => setQuery((prev) => ({ ...prev, category: e.target.value || undefined, page: 0 }))}
        />
        <input
          className="control"
          placeholder="설비 유형"
          value={query.equipmentType ?? ''}
          onChange={(e) => setQuery((prev) => ({ ...prev, equipmentType: e.target.value || undefined, page: 0 }))}
        />
      </div>

      <DocumentTable
        items={data?.content ?? []}
        loading={loading}
        error={error}
        onEdit={(id) => navigate(`/documents/${id}/edit`)}
        onDelete={onDelete}
      />
      <div className="page-panel flex items-center justify-between">
        <span className="text-sm text-slate-600">
          페이지 {(data?.page ?? 0) + 1} / {Math.max(data?.totalPages ?? 1, 1)} (총 {data?.totalElements ?? 0}건)
        </span>
        <div className="flex gap-2">
          <button
            className="btn-secondary"
            disabled={(data?.page ?? 0) <= 0 || loading}
            onClick={() => setQuery((prev) => ({ ...prev, page: Math.max((prev.page ?? 0) - 1, 0) }))}
          >
            이전
          </button>
          <button
            className="btn-secondary"
            disabled={loading || (data ? data.page + 1 >= data.totalPages : true)}
            onClick={() => setQuery((prev) => ({ ...prev, page: (prev.page ?? 0) + 1 }))}
          >
            다음
          </button>
        </div>
      </div>
    </section>
  )
}
