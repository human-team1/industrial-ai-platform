import { Link, useNavigate } from 'react-router-dom'
import { useChatHistory } from '../../features/chatbot/model'
import { ChatConversationList, ChatDetailPanel } from '../../features/chatbot/ui'

export function ChatHistoryPanel() {
  const navigate = useNavigate()
  const history = useChatHistory()
  const page = history.data
  const emptyMessage = history.isSearchResultEmpty
    ? '검색 조건에 맞는 대화가 없습니다.'
    : '저장된 챗봇 대화가 없습니다. 새 질문을 시작해보세요.'

  const deleteConversation = async (conversationId: number) => {
    if (!window.confirm('챗봇 대화를 삭제하시겠습니까?')) return
    await history.removeConversation(conversationId)
  }

  return (
    <section className="space-y-5">
      <div className="page-panel flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">챗봇 히스토리</h1>
          <p className="mt-2 text-sm text-slate-600">이전 문서 기반 질의응답 이력을 조회합니다.</p>
        </div>
        <Link className="btn-primary inline-flex items-center" to="/chatbot">
          새 대화 시작
        </Link>
      </div>

      <div className="page-panel grid grid-cols-1 gap-3 md:grid-cols-4">
        <input
          className="control md:col-span-2"
          placeholder="대화 제목 또는 메시지 검색"
          value={history.query.keyword ?? ''}
          onChange={(event) =>
            history.setQuery((prev) => ({ ...prev, keyword: event.target.value || undefined, page: 0 }))
          }
        />
        <input
          className="control"
          type="date"
          value={history.query.from ?? ''}
          onChange={(event) =>
            history.setQuery((prev) => ({ ...prev, from: event.target.value || undefined, page: 0 }))
          }
        />
        <input
          className="control"
          type="date"
          value={history.query.to ?? ''}
          onChange={(event) =>
            history.setQuery((prev) => ({ ...prev, to: event.target.value || undefined, page: 0 }))
          }
        />
      </div>

      {history.error ? (
        <div className="rounded-md border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          <div className="flex items-center justify-between gap-3">
            <span>{history.error}</span>
            <button className="btn-secondary" type="button" onClick={history.reload}>
              재시도
            </button>
          </div>
        </div>
      ) : null}

      <div className="grid gap-5 lg:grid-cols-[420px_minmax(0,1fr)]">
        <div className="space-y-3">
          <ChatConversationList
            items={page?.content ?? []}
            selectedId={history.selected?.conversationId}
            loading={history.loading}
            emptyMessage={emptyMessage}
            onSelect={history.selectConversation}
            onDelete={deleteConversation}
          />
          <div className="page-panel flex items-center justify-between">
            <span className="text-sm text-slate-600">
              페이지 {(page?.page ?? 0) + 1} / {Math.max(page?.totalPages ?? 1, 1)} (총 {page?.totalElements ?? 0}건)
            </span>
            <div className="flex gap-2">
              <button
                className="btn-secondary"
                disabled={(page?.page ?? 0) <= 0 || history.loading}
                onClick={() => history.setQuery((prev) => ({ ...prev, page: Math.max(prev.page - 1, 0) }))}
                type="button"
              >
                이전
              </button>
              <button
                className="btn-secondary"
                disabled={history.loading || (page ? page.page + 1 >= page.totalPages : true)}
                onClick={() => history.setQuery((prev) => ({ ...prev, page: prev.page + 1 }))}
                type="button"
              >
                다음
              </button>
            </div>
          </div>
        </div>
        <div className="space-y-3">
          {history.selected ? (
            <div className="flex justify-end">
              <button
                className="btn-secondary"
                type="button"
                onClick={() => navigate(`/chatbot?conversationId=${history.selected?.conversationId}`)}
              >
                대화 열기
              </button>
            </div>
          ) : null}
          <ChatDetailPanel detail={history.selected} loading={history.detailLoading} />
        </div>
      </div>
    </section>
  )
}
