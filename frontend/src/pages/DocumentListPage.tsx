import { useNavigate } from 'react-router-dom'
import { removeDocument, useDocumentList } from '../features/document/model'
import { DocumentSummaryCards, DocumentTable } from '../features/document/ui'

export function DocumentListPage() {
  const navigate = useNavigate()
  const { data, summary, loading, error, reload } = useDocumentList()

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

      <DocumentTable
        items={data?.content ?? []}
        loading={loading}
        error={error}
        onEdit={(id) => navigate(`/documents/${id}/edit`)}
        onDelete={onDelete}
      />
    </section>
  )
}
