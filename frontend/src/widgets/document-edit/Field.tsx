import type { ReactNode } from 'react'

type Props = {
  label: string
  required?: boolean
  errorMessage?: string
  className?: string
  children: ReactNode
}

export function Field({ label, required, errorMessage, className = '', children }: Props) {
  return (
    <label className={`block space-y-2 ${className}`}>
      <span className="block text-sm font-medium text-slate-700">
        {label}
        {required ? <span className="text-red-500"> *</span> : null}
      </span>
      {children}
      {errorMessage ? <span className="block text-sm text-red-600">{errorMessage}</span> : null}
    </label>
  )
}
