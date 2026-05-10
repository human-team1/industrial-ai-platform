import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { removeDocument, useDocumentList } from '../features/document/model'
import { DocumentFilterForm, DocumentSearchBar } from '../features/document/ui'
import {
  buildDocumentSummaryView,
  DocumentSummaryCards,
  DocumentTable,
} from '../widgets/document-list'
import { PaginationBar } from '../shared/ui/pagination/PaginationBar'

export function DocumentListPage() {
  const navigate = useNavigate()
  const { draft, setDraft, data, loading, error, search, resetFilters, setPage, reload } =
    useDocumentList()

  const summary = useMemo(
    () => (data ? buildDocumentSummaryView(data.content) : null),
    [data],
  )

  const onDelete = async (documentId: number) => {
    if (!window.confirm('문서를 삭제하시겠습니까?')) return
    try {
      await removeDocument(documentId)
      await reload()
    } catch (err) {
      window.alert(err instanceof Error ? err.message : '삭제에 실패했습니다.')
    }
  }

  return (
    <section className="space-y-5">
      <div className="page-panel flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">문서 관리</h1>
          <p className="mt-2 text-sm text-slate-600">
            RAG 문서 목록, 인덱싱 상태, 등록/수정/삭제를 관리합니다.
          </p>
        </div>
        <button type="button" className="btn-primary" onClick={() => navigate('/documents/new')}>
          문서 등록
        </button>
      </div>

      <DocumentSearchBar
        draft={draft}
        loading={loading}
        onChange={setDraft}
        onSearch={search}
      />

      <DocumentFilterForm
        draft={draft}
        loading={loading}
        onChange={setDraft}
        onSearch={search}
        onReset={resetFilters}
      />

      <DocumentSummaryCards summary={summary} loading={loading && !summary} />

      <DocumentTable
        items={data?.content ?? []}
        loading={loading}
        error={error}
        onView={(id) => navigate(`/documents/${id}/edit`)}
        onEdit={(id) => navigate(`/documents/${id}/edit`)}
        onDelete={onDelete}
        onRegister={() => navigate('/documents/new')}
        onRetry={() => void reload()}
        onShowFailureReason={(id) => navigate(`/documents/${id}/edit`)}
      />

      {data ? (
        <PaginationBar
          page={data.page}
          totalPages={data.totalPages}
          totalElements={data.totalElements}
          loading={loading}
          onPageChange={setPage}
        />
      ) : null}
    </section>
  )
}
