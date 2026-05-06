const PLACEHOLDER = '-'

export function formatDate(iso?: string): string {
  if (!iso) return PLACEHOLDER
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return PLACEHOLDER
  return d.toISOString().slice(0, 10)
}

export function formatDateTime(iso?: string): string {
  if (!iso) return PLACEHOLDER
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return PLACEHOLDER
  const date = d.toISOString().slice(0, 10)
  const time = d.toTimeString().slice(0, 5)
  return `${date} ${time}`
}

export function formatElapsedTime(ms: number | null): string {
  if (ms == null) return '00:00:00'
  const totalSeconds = Math.max(0, Math.floor(ms / 1000))
  const hh = String(Math.floor(totalSeconds / 3600)).padStart(2, '0')
  const mm = String(Math.floor((totalSeconds % 3600) / 60)).padStart(2, '0')
  const ss = String(totalSeconds % 60).padStart(2, '0')
  return `${hh}:${mm}:${ss}`
}
