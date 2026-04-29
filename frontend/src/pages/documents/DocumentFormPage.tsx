import { useNavigate, useParams } from 'react-router-dom'
import { useDocumentForm } from '../../features/document-form/model'
import { DocumentForm } from '../../features/document-form/ui'

export function DocumentFormPage() {
  const navigate = useNavigate()
  const rawDocumentId = useParams().documentId
  const documentId = rawDocumentId ? Number(rawDocumentId) : undefined
  const form = useDocumentForm(Number.isFinite(documentId) ? documentId : undefined)

  const onSubmit = async () => {
    const result = await form.submit()
    if (result) {
      navigate('/documents')
    }
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
