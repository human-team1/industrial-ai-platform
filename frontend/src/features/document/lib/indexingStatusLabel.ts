import type { IndexingStatus } from '../types'

export function indexingStatusLabel(status: IndexingStatus | string | undefined | null): string {
  if (!status) return '-'
  const value = String(status).toUpperCase()
  if (value === 'PENDING') return '대기'
  if (value === 'PROCESSING') return '처리중'
  if (value === 'COMPLETED') return '반영완료'
  if (value === 'FAILED') return '반영실패'
  return String(status)
}
