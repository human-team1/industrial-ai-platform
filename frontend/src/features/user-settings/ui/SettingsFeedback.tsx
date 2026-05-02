import type { SaveStatus } from '../model/useUserSettingsForm'

type Props = {
  loading: boolean
  loadError: string | null
  saveStatus: SaveStatus
  validationError: string | null
}

export function SettingsFeedback({ loading, loadError, saveStatus, validationError }: Props) {
  return (
    <>
      {loading && (
        <div className="mb-4 flex items-center gap-2 px-4 py-2.5 bg-[#eef3fb] border border-[#bcd0ec] rounded text-[#1f4f8a] text-sm w-fit">
          설정을 불러오는 중...
        </div>
      )}

      {loadError && (
        <div className="mb-4 flex items-start gap-2 px-4 py-2.5 bg-[#fcebea] border border-[#f3b9b6] rounded text-[#a13b34] text-sm w-fit">
          {loadError}
        </div>
      )}

      {saveStatus.kind === 'success' && (
        <div className="mb-4 flex items-center gap-2 px-4 py-2.5 bg-[#edf6ed] border border-[#b7ddb7] rounded text-[#3d7a3d] text-sm w-fit">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 16 16" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M3 8l4 4 6-6" />
          </svg>
          설정이 저장되었습니다.
        </div>
      )}

      {saveStatus.kind === 'noop' && (
        <div className="mb-4 flex items-center gap-2 px-4 py-2.5 bg-[#f4f5f7] border border-[#d8dbe1] rounded text-[#4b5563] text-sm w-fit">
          변경된 설정이 없습니다.
        </div>
      )}

      {saveStatus.kind === 'partial' && (
        <div className="mb-4 px-4 py-2.5 bg-[#fff8e1] border border-[#f3d27a] rounded text-[#8a6d1c] text-sm">
          <div className="font-medium">일부 설정만 저장되었습니다.</div>
          <ul className="list-disc pl-5 mt-1 text-xs">
            {saveStatus.messages.map((m) => (
              <li key={m}>{m}</li>
            ))}
          </ul>
        </div>
      )}

      {saveStatus.kind === 'error' && (
        <div className="mb-4 px-4 py-2.5 bg-[#fcebea] border border-[#f3b9b6] rounded text-[#a13b34] text-sm">
          <div className="font-medium">설정 저장에 실패했습니다.</div>
          <ul className="list-disc pl-5 mt-1 text-xs">
            {saveStatus.messages.map((m) => (
              <li key={m}>{m}</li>
            ))}
          </ul>
        </div>
      )}

      {validationError && saveStatus.kind !== 'error' && (
        <div className="mb-4 px-4 py-2.5 bg-[#fcebea] border border-[#f3b9b6] rounded text-[#a13b34] text-sm w-fit">
          {validationError}
        </div>
      )}
    </>
  )
}
