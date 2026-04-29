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
