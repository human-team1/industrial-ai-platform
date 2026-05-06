import type { DocumentFormActionsProps } from './types'

export function DocumentFormActions({
  mode,
  saving,
  submitLabel,
  onCancel,
  onSubmit,
  onDelete,
}: DocumentFormActionsProps) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3">
      <button
        type="button"
        className="btn-secondary inline-flex min-w-[5rem] items-center justify-center whitespace-nowrap px-5"
        onClick={onCancel}
        disabled={saving}
      >
        취소
      </button>
      <button
        type="button"
        className="btn-primary inline-flex min-w-[8rem] items-center justify-center whitespace-nowrap px-6"
        disabled={saving}
        onClick={onSubmit}
      >
        {saving ? '저장 중...' : submitLabel}
      </button>
      <button
        type="button"
        className="inline-flex min-w-[5.5rem] items-center justify-center whitespace-nowrap rounded border border-rose-200 bg-white px-5 py-2 text-sm font-semibold text-rose-600 hover:bg-rose-50 disabled:opacity-50"
        disabled={saving || mode !== 'edit'}
        onClick={onDelete}
        title={
          mode === 'edit'
            ? '문서 삭제 (준비 중)'
            : '신규 등록 화면에서는 사용할 수 없습니다.'
        }
      >
        문서 삭제
      </button>
    </div>
  )
}
