/** 운영 모니터링 / 관리자 화면 공통 표시 */

export const UNAVAILABLE = '수집 불가'

export function formatUsagePercent(value: number | null | undefined): string {
  if (value == null || !Number.isFinite(value)) return UNAVAILABLE
  const n = Math.round(value * 100) / 100
  const s = Number.isInteger(n) ? String(n) : n.toFixed(2).replace(/\.?0+$/, '')
  return `${s}%`
}

export function formatResponseTimeMs(value: number | null | undefined): string {
  if (value == null || !Number.isFinite(value)) return UNAVAILABLE
  return `${Math.round(value)}ms`
}

const STATUS_LABELS: Record<string, string> = {
  NORMAL: '정상',
  WARNING: '주의',
  WARN: '주의',
  INFO: '정보',
  ERROR: '오류',
  UNKNOWN: '확인 불가',
  ACTIVE: '활성',
  INACTIVE: '비활성',
  RUNNING: '실행 중',
  COMPLETED: '완료',
  FAILED: '실패',
  PROCESSING: '처리 중',
  PENDING: '대기',
  SUCCESS: '성공',
  SLOW: '지연',
}

export function statusBadgeLabel(code: string | null | undefined): string {
  if (code == null || code === '') return STATUS_LABELS.UNKNOWN
  const upper = code.toUpperCase()
  return STATUS_LABELS[upper] ?? code
}

export function policyCategoryLabel(category: string | null | undefined): string {
  if (!category) return '-'
  const map: Record<string, string> = {
    INSPECTION: '판정/검사 기준',
    NOTIFICATION: '알림 기준',
    SECURITY: '보안 기준',
    RETENTION: '보존 정책',
    SYSTEM: '운영 정책',
  }
  return map[category] ?? category
}

export const SOURCE_COMPONENT_OPTIONS = [
  { value: '', label: '전체' },
  { value: 'SPRING_API', label: 'SPRING_API' },
  { value: 'AI_SERVER', label: 'AI_SERVER' },
  { value: 'MARIADB', label: 'MARIADB' },
  { value: 'REDIS', label: 'REDIS' },
  { value: 'MINIO', label: 'MINIO' },
  { value: 'CHROMA', label: 'CHROMA' },
  { value: 'STREAM_SERVER', label: 'STREAM_SERVER' },
  { value: 'STORAGE', label: 'STORAGE' },
] as const
