import type { ChatHistoryHeaderProps } from './types'

export function ChatHistoryHeader({ onStartNew }: ChatHistoryHeaderProps) {
  return (
    <div className="page-panel">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="min-w-0">
          <h1 className="text-2xl font-semibold text-slate-900">챗봇 히스토리</h1>
          <p className="mt-2 text-sm leading-relaxed text-slate-600">
            이전 문서 기반 질의응답 이력을 조회합니다.
          </p>
        </div>
        <button
          type="button"
          className="btn-primary inline-flex h-10 shrink-0 items-center justify-center whitespace-nowrap self-start sm:self-center"
          onClick={onStartNew}
        >
          새 대화 시작
        </button>
      </div>
    </div>
  )
}
