import { useNavigate } from 'react-router-dom'
import { removeDocument, useDocumentList } from '../features/document/model'
import {
  toDocumentIndexingStatusChart,
  toDocumentPageStatusChart,
  toDocumentTypeChart,
} from '../features/document/model/chartViewModels'
import { DocumentSummaryCards, DocumentTable } from '../features/document/ui'
import { BarChartCard } from '../shared/ui/chart/BarChartCard'
import { PaginationBar } from '../shared/ui/pagination/PaginationBar'

export function DocumentListPage() {
  const navigate = useNavigate()
  const { draft, setDraft, data, summary, loading, error, search, resetFilters, setPage, reload } =
    useDocumentList()

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

      <section className="page-panel">
        <div className="relative w-full lg:w-1/3">
          <input
            className="control w-full pr-10"
            placeholder="제목/설명/태그 검색"
            value={draft.keyword ?? ''}
            onChange={(event) => setDraft((prev) => ({ ...prev, keyword: event.target.value }))}
            onKeyDown={(event) => {
              if (event.key === 'Enter') search()
            }}
          />
          <button
            type="button"
            onClick={search}
            disabled={loading}
            aria-label="검색"
            className="absolute inset-y-0 right-0 flex w-10 items-center justify-center text-slate-500 hover:text-slate-900 disabled:opacity-50"
          >
            <svg
              className="h-4 w-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth={1.8}
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <circle cx="11" cy="11" r="7" />
              <path d="M20 20l-3.5-3.5" />
            </svg>
          </button>
        </div>
      </section>

      <section className="page-panel">
        <div className="grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_auto_auto]">
          <select
            className="control min-w-0"
            value={draft.documentType ?? ''}
            onChange={(event) =>
              setDraft((prev) => ({ ...prev, documentType: event.target.value || undefined }))
            }
          >
            <option value="">전체 유형</option>
            <option value="PDF">PDF</option>
            <option value="DOCX">DOCX</option>
            <option value="MD">MD</option>
          </select>
          <input
            className="control min-w-0"
            placeholder="카테고리"
            value={draft.category ?? ''}
            onChange={(event) => setDraft((prev) => ({ ...prev, category: event.target.value }))}
          />
          <input
            className="control min-w-0"
            placeholder="설비 유형"
            value={draft.equipmentType ?? ''}
            onChange={(event) => setDraft((prev) => ({ ...prev, equipmentType: event.target.value }))}
          />
          <select
            className="control min-w-0"
            value={draft.indexingStatus ?? ''}
            onChange={(event) =>
              setDraft((prev) => ({ ...prev, indexingStatus: event.target.value || undefined }))
            }
          >
            <option value="">전체 반영 상태</option>
            <option value="PENDING">대기</option>
            <option value="PROCESSING">처리중</option>
            <option value="COMPLETED">반영완료</option>
            <option value="FAILED">반영실패</option>
          </select>
          <input
            className="control min-w-0"
            type="date"
            value={draft.startDate ?? ''}
            onChange={(event) => setDraft((prev) => ({ ...prev, startDate: event.target.value }))}
          />
          <input
            className="control min-w-0"
            type="date"
            value={draft.endDate ?? ''}
            onChange={(event) => setDraft((prev) => ({ ...prev, endDate: event.target.value }))}
          />
          <button type="button" className="btn-secondary whitespace-nowrap" disabled={loading} onClick={resetFilters}>
            필터 초기화
          </button>
          <button type="button" className="btn-primary whitespace-nowrap" disabled={loading} onClick={search}>
            검색
          </button>
        </div>
      </section>

      <DocumentSummaryCards summary={summary} loading={loading && !summary} />

      <div className="grid grid-cols-1 gap-5 lg:grid-cols-3">
        <BarChartCard
          viewModel={toDocumentIndexingStatusChart(summary)}
          loading={loading && !summary}
          empty={{ reason: 'NO_DATA', message: '문서 상태 데이터가 없습니다.' }}
        />
        <BarChartCard
          viewModel={toDocumentTypeChart(data?.content ?? [])}
          loading={loading && !data}
          empty={{ reason: 'NO_DATA', message: '현재 페이지에 문서가 없습니다.' }}
        />
        <BarChartCard
          viewModel={toDocumentPageStatusChart(data?.content ?? [])}
          loading={loading && !data}
          empty={{ reason: 'NO_DATA', message: '현재 페이지에 문서 상태가 없습니다.' }}
        />
      </div>

      <DocumentTable
        items={data?.content ?? []}
        loading={loading}
        error={error}
        onView={(id) => navigate(`/documents/${id}/edit`)}
        onEdit={(id) => navigate(`/documents/${id}/edit`)}
        onDelete={onDelete}
        onRegister={() => navigate('/documents/new')}
        onRetry={() => void reload()}
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
