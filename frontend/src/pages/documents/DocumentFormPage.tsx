import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useDocumentForm } from '../../features/document-form/model'
import {
  DocumentFileUpload,
  DocumentFormActions,
  DocumentIndexingStatus,
  DocumentMetadataFields,
  DocumentPreviewPanel,
} from '../../widgets/document-edit'
import type { DocumentCreateResult } from '../../features/document/types'
import { indexingStatusLabel } from '../../features/document/lib/indexingStatusLabel'

export function DocumentFormPage() {
  const navigate = useNavigate()
  const rawDocumentId = useParams().documentId
  const documentId = rawDocumentId ? Number(rawDocumentId) : undefined
  const isCreate = !rawDocumentId
  const form = useDocumentForm(Number.isFinite(documentId) ? documentId : undefined)
  const [createResult, setCreateResult] = useState<DocumentCreateResult | null>(null)

  const onSubmit = async () => {
    const result = await form.submit()
    if (!result) return
    if (isCreate) {
      setCreateResult(result as DocumentCreateResult)
      return
    }
    navigate('/documents')
  }

  const onDelete = () => {
    window.alert('문서 삭제 기능은 준비 중입니다. (UI 전용)')
  }

  if (createResult) {
    return (
      <section className="mx-auto w-full max-w-[min(100%,48rem)] space-y-5">
        <div className="page-panel space-y-4 border-emerald-200 bg-emerald-50">
          <h1 className="text-xl font-semibold text-emerald-900">문서가 등록되었습니다</h1>
          <p className="text-sm text-emerald-800">
            문서 ID {createResult.documentId}
            {createResult.documentVersionId != null ? ` · 버전 ID ${createResult.documentVersionId}` : ''}
          </p>
          <p className="text-sm text-emerald-800">
            인덱싱 상태: <strong>{indexingStatusLabel(createResult.indexingStatus)}</strong>
          </p>
          <p className="text-xs text-emerald-700">
            위 상태는 Spring 등록 API가 반환한 값입니다. AI 서버(FastAPI)·RAG 인덱싱 완료 여부는 별도 인프라 연동 후에야 검증할 수 있습니다.
          </p>
          <div className="flex flex-wrap gap-3 pt-2">
            <button
              type="button"
              className="btn-primary inline-flex items-center justify-center whitespace-nowrap px-5"
              onClick={() => navigate('/documents')}
            >
              문서 목록으로
            </button>
            <button
              type="button"
              className="btn-secondary inline-flex items-center justify-center whitespace-nowrap px-5"
              onClick={() => navigate(`/documents/${createResult.documentId}/edit`)}
            >
              문서 상세·수정
            </button>
          </div>
        </div>
      </section>
    )
  }

  if (form.loading) return <div className="page-panel">문서 정보를 불러오는 중입니다.</div>

  if (form.mode === 'edit' && form.errorMessage && !form.detail) {
    return <div className="page-panel text-sm text-red-600">{form.errorMessage}</div>
  }

  return (
    <section className="mx-auto w-full max-w-[min(100%,72rem)] space-y-5">
      <div className="page-panel">
        <h1 className="text-2xl font-semibold text-slate-900">문서 등록 및 수정</h1>
        <p className="mt-2 text-sm leading-relaxed text-slate-600">{form.pageText.description}</p>
      </div>

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-2">
        <section className="page-panel">
          <DocumentFileUpload
            mode={form.mode}
            file={form.file}
            latestVersion={form.detail?.latestVersion ?? null}
            errorMessage={form.fieldErrors.file}
            onFileChange={form.setSelectedFile}
          />
        </section>
        <section className="page-panel">
          <DocumentIndexingStatus mode={form.mode} latestVersion={form.detail?.latestVersion ?? null} />
        </section>
        <section className="page-panel">
          <DocumentMetadataFields
            values={form.values}
            fieldErrors={form.fieldErrors}
            onChange={form.setFieldValue}
          />
        </section>
        <section className="page-panel">
          <DocumentPreviewPanel
            mode={form.mode}
            latestVersion={form.detail?.latestVersion ?? null}
            loading={form.previewLoading}
            errorMessage={form.previewErrorMessage}
            onPreviewOpen={() => void form.openPreview()}
          />
        </section>
      </div>

      {form.errorMessage ? (
        <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {form.errorMessage}
        </p>
      ) : null}

      <DocumentFormActions
        mode={form.mode}
        saving={form.saving}
        submitLabel={form.pageText.submitLabel}
        onCancel={() => navigate('/documents')}
        onSubmit={() => void onSubmit()}
        onDelete={onDelete}
      />
    </section>
  )
}
