import { useEffect, useState } from 'react'

type Variant = 'info' | 'warning' | 'error'

const VARIANT_CLASS: Record<Variant, string> = {
  info: 'bg-[#eef4ff] border-[#bcd2ff] text-[#1166e0]',
  warning: 'bg-[#fff7e6] border-[#ffd591] text-[#a8530b]',
  error: 'bg-[#fdecec] border-[#f5b5b5] text-[#b42318]',
}

type Props = {
  message: string
  variant?: Variant
  autoCloseMs?: number
  onClose?: () => void
}

export function NoticeBanner({ message, variant = 'info', autoCloseMs = 4000, onClose }: Props) {
  const [open, setOpen] = useState(true)

  useEffect(() => {
    if (!autoCloseMs) return
    const timer = setTimeout(() => {
      setOpen(false)
      onClose?.()
    }, autoCloseMs)
    return () => clearTimeout(timer)
  }, [autoCloseMs, onClose])

  if (!open) return null

  return (
    <div
      role="status"
      className={`fixed top-4 left-1/2 -translate-x-1/2 z-50 px-4 py-2.5 rounded border text-sm shadow-md ${VARIANT_CLASS[variant]}`}
    >
      {message}
    </div>
  )
}
