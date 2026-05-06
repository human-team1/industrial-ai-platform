import type { DocumentPreviewPanelProps } from './types'

export function DocumentPreviewPanel({
  mode,
  latestVersion,
  loading,
  errorMessage,
  onPreviewOpen,
}: DocumentPreviewPanelProps) {
  if (mode === 'create') {
    return (
      <section className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-800">문서 미리보기</h2>
        <div className="flex min-h-[100px] items-center rounded-lg border border-slate-200 bg-slate-50 px-5 py-4 text-sm leading-relaxed text-slate-600">
          저장 후 미리보기를 확인할 수 있습니다.
        </div>
      </section>
    )
  }

  const canPreview = Boolean(latestVersion?.fileId)

  return (
    <section className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-sm font-semibold text-slate-800">문서 미리보기</h2>
        {canPreview ? (
          <button
            type="button"
            className="btn-secondary inline-flex shrink-0 items-center justify-center whitespace-nowrap px-4"
            onClick={onPreviewOpen}
            disabled={loading}
          >
            {loading ? '불러오는 중...' : '새 탭에서 열기'}
          </button>
        ) : null}
      </div>
      <div className="min-h-[128px] max-h-72 overflow-y-auto rounded-lg border border-slate-200 bg-slate-50 p-5 text-sm leading-6 text-slate-600">
        {errorMessage ??
          (canPreview
            ? '백엔드가 텍스트 미리보기 필드를 제공하지 않아 원문은 새 탭에서 확인할 수 있습니다.'
            : '미리보기 데이터를 제공하는 API가 없습니다.')}
      </div>
    </section>
  )
}
