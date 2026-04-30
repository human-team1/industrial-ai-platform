import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useDocumentForm } from '../../features/document-form/model'
import { DocumentForm } from '../../features/document-form/ui'
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

  if (createResult) {
    return (
      <section className="mx-auto w-full max-w-2xl space-y-4">
        <div className="page-panel space-y-3 border-emerald-200 bg-emerald-50">
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
          <div className="flex flex-wrap gap-2 pt-2">
            <button type="button" className="btn-primary" onClick={() => navigate('/documents')}>
              문서 목록으로
            </button>
            <button
              type="button"
              className="btn-secondary"
              onClick={() => navigate(`/documents/${createResult.documentId}/edit`)}
            >
              문서 상세·수정
            </button>
          </div>
        </div>
      </section>
    )
  }

  return (
    <DocumentForm
      mode={form.mode}
      title={form.pageText.title}
      description={form.pageText.description}
      submitLabel={form.pageText.submitLabel}
      values={form.values}
      file={form.file}
      detail={form.detail}
      loading={form.loading}
      saving={form.saving}
      previewLoading={form.previewLoading}
      previewErrorMessage={form.previewErrorMessage}
      errorMessage={form.errorMessage}
      fieldErrors={form.fieldErrors}
      onChange={form.setFieldValue}
      onFileChange={form.setSelectedFile}
      onCancel={() => navigate('/documents')}
      onPreviewOpen={() => void form.openPreview()}
      onSubmit={() => void onSubmit()}
    />
  )
}
