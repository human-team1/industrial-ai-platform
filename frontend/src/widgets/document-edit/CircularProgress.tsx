import type { CircularProgressProps } from './types'

export function CircularProgress({ percent, status }: CircularProgressProps) {
  const clamped = Math.max(0, Math.min(100, percent))
  const color =
    status === 'FAILED' ? '#f43f5e' : status === 'COMPLETED' ? '#10b981' : '#0ea5e9'
  const bg = `conic-gradient(${color} ${clamped * 3.6}deg, #e2e8f0 0deg)`
  return (
    <div className="flex h-24 w-24 shrink-0 items-center justify-center rounded-full" style={{ background: bg }}>
      <div className="flex h-[4.5rem] w-[4.5rem] flex-col items-center justify-center rounded-full bg-white">
        <span className="text-lg font-bold text-slate-900">{clamped}%</span>
        <span className="text-[10px] text-slate-500">진행률</span>
      </div>
    </div>
  )
}
