export function ChevronRight({ className = 'w-3 h-3' }: { className?: string }) {
  return (
    <svg className={className} fill="none" viewBox="0 0 12 12" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M4 2l4 4-4 4" />
    </svg>
  )
}
