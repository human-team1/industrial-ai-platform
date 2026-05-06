import type { IndexingStatus } from './types'

export function labelIndexingStatus(status: IndexingStatus | string | undefined | null): string {
  if (!status) return '-'
  const value = String(status).toUpperCase()
  if (value === 'PENDING') return '대기'
  if (value === 'PROCESSING') return '처리중'
  if (value === 'COMPLETED') return '반영완료'
  if (value === 'FAILED') return '반영실패'
  return String(status)
}

export function labelDocumentType(type: string | undefined | null): string {
  if (!type) return '-'
  const value = String(type).toUpperCase()
  if (value === 'PDF') return 'PDF'
  if (value === 'DOCX') return 'DOCX'
  if (value === 'MD') return 'Markdown'
  return String(type)
}

export function formatDocumentStatus(status: string | undefined | null): string {
  return labelIndexingStatus(status)
}
