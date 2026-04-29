export function SettingsActions({
  onSave,
  onCancel,
  onReset,
}: {
  onSave: () => void
  onCancel: () => void
  onReset: () => void
}) {
  return (
    <div className="bg-[#fdfdfd] border-t-[6px] border-[#fafafc] px-5 py-3 flex items-center justify-between">
      <button
        type="button"
        onClick={onReset}
        className="flex items-center gap-2 px-4 py-2 bg-[#fdfdfd] border border-[#f3f4f7] rounded-[3px] text-[#4b5563] text-xs hover:bg-[#f5f6fa] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]"
      >
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 14 14" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M1 7a6 6 0 106-6H3M3 1v3h3" />
        </svg>
        기본값으로 복원
      </button>

      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={onCancel}
          className="px-7 py-2 bg-[#fffefe] border border-[#e3e1e0] rounded-[5.75px] text-[#4b5563] text-xs hover:bg-[#f5f6fa] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]"
        >
          취소
        </button>
        <button
          type="submit"
          onClick={onSave}
          className="flex items-center gap-1.5 px-7 py-2 bg-[#0965e3] border border-[#0458e5] rounded-[5.25px] text-[#88b5ef] text-[13px] hover:bg-[#0755cc] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]"
        >
          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 14 14" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M2 7l4 4 6-6" />
          </svg>
          저장
        </button>
      </div>
    </div>
  )
}
