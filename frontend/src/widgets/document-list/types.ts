import type { Document } from '../../entities/document/model/types'

export type DocumentSummaryView = {
  totalCount: number
  pdfCount: number
  docxCount: number
  completedCount: number
  processingCount: number
  failedCount: number
}

export type DocumentTableRow = Document

export type DocumentSummaryCardsProps = {
  summary: DocumentSummaryView | null
  loading?: boolean
}

export type DocumentTableProps = {
  items: DocumentTableRow[]
  loading: boolean
  error: string | null
  onView: (id: number) => void
  onEdit: (id: number) => void
  onDelete: (id: number) => void
  onRegister: () => void
  onRetry?: () => void
  onShowFailureReason?: (id: number) => void
}
