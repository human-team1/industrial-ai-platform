import type { DocumentVersion, IndexingStatus } from '../../entities/document/model/types'

// widget 내부 표시용 폼 ViewModel — features/document-form 도메인 타입과 형태는 동일하지만
// widget 레이어 격리를 위해 자체 정의 (의존 방향: widgets → entities/shared만)

export type DocumentFormMode = 'create' | 'edit'

export type DocumentFormValues = {
  title: string
  category: string
  equipmentType: string
  tags: string
  description: string
}

export type DocumentFormFieldErrors = Partial<Record<keyof DocumentFormValues | 'file', string>>

export type DocumentFileUploadProps = {
  mode: DocumentFormMode
  file: File | null
  latestVersion: DocumentVersion | null
  errorMessage?: string
  onFileChange: (file: File | null) => void
}

export type DocumentMetadataFieldsProps = {
  values: DocumentFormValues
  fieldErrors: DocumentFormFieldErrors
  onChange: (name: keyof DocumentFormValues, value: string) => void
}

export type DocumentIndexingStatusProps = {
  mode: DocumentFormMode
  latestVersion: DocumentVersion | null
}

export type DocumentPreviewPanelProps = {
  mode: DocumentFormMode
  latestVersion: DocumentVersion | null
  loading: boolean
  errorMessage: string | null
  onPreviewOpen: () => void
}

export type DocumentFormActionsProps = {
  mode: DocumentFormMode
  saving: boolean
  submitLabel: string
  onCancel: () => void
  onSubmit: () => void
  onDelete: () => void
}

export type IndexingStepperProps = {
  activeStep: number
  failed: boolean
}

export type CircularProgressProps = {
  percent: number
  status: IndexingStatus | null
}
