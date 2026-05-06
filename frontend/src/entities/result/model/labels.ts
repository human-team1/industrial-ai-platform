const decisionLabels: Record<string, string> = {
  NORMAL: '정상',
  DEFECT: '이상',
  RETEST: '재검사',
  RECHECK: '재검사',
  REINSPECTION: '재검사',
}

export function labelDecision(value?: string | null): string {
  if (!value) return '-'
  return decisionLabels[value] ?? value
}

export function labelRunType(value?: string | null): string {
  if (!value) return '-'
  if (value === 'REALTIME') return '실시간 검사'
  if (['UPLOAD', 'IMAGE_UPLOAD', 'VIDEO_UPLOAD'].includes(value)) return '업로드 검사'
  return value
}

export function labelSourceType(value?: string | null): string {
  if (!value) return '-'
  if (value === 'BROWSER_CAMERA') return '카메라 캡처'
  if (['IMAGE', 'UPLOAD', 'IMAGE_UPLOAD', 'VIDEO_UPLOAD'].includes(value)) return '이미지 업로드'
  return value
}

export function formatResultStatus(value?: string | null): string {
  if (!value) return '-'
  const map: Record<string, string> = {
    SUCCESS: '성공',
    FAILED: '실패',
    REVIEW_REQUIRED: '재검토 대상',
    CORRECTED: '수정완료',
  }
  return map[value] ?? value
}

export function formatDateMinute(value?: string | null): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mi = String(date.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}
