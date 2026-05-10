import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { useChatHistory } from '../features/chatbot/model'
import {
  buildHistorySummary,
  buildRecentItems,
  ChatConversationList,
  ChatDetailPanel,
  ChatHistoryFilterBar,
  ChatHistoryHeader,
  ChatHistorySidebar,
} from '../widgets/chatbot-history'
import { PaginationBar } from '../shared/ui/pagination/PaginationBar'

export function ChatbotHistoryPage() {
  const navigate = useNavigate()
  const history = useChatHistory()
  const page = history.data
  const items = page?.content ?? []

  const summary = useMemo(
    () => (page ? buildHistorySummary(items, page.totalElements) : null),
    [items, page],
  )
  const recent = useMemo(() => buildRecentItems(items), [items])

  const emptyMessage = history.isSearchResultEmpty
    ? '검색 조건에 맞는 대화가 없습니다.'
    : '저장된 챗봇 대화가 없습니다. 새 질문을 시작해보세요.'

  return (
    <section className="space-y-5">
      <ChatHistoryHeader onStartNew={() => navigate('/chatbot')} />

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

      <div className="grid gap-5 xl:grid-cols-5">
        <div className="flex min-h-0 flex-col gap-4 xl:col-span-2">
          <ChatHistoryFilterBar
            keyword={history.query.keyword ?? ''}
            from={history.query.from ?? ''}
            to={history.query.to ?? ''}
            onKeywordChange={(value) =>
              history.setQuery((prev) => ({ ...prev, keyword: value || undefined, page: 0 }))
            }
            onFromChange={(value) =>
              history.setQuery((prev) => ({ ...prev, from: value || undefined, page: 0 }))
            }
            onToChange={(value) =>
              history.setQuery((prev) => ({ ...prev, to: value || undefined, page: 0 }))
            }
            onSearch={() => void history.reload()}
          />
          <ChatConversationList
            items={items}
            selectedId={history.selected?.conversationId}
            loading={history.loading}
            emptyMessage={emptyMessage}
            onSelect={history.selectConversation}
          />
          {page ? (
            <PaginationBar
              page={page.page}
              totalPages={page.totalPages}
              totalElements={page.totalElements}
              loading={history.loading}
              onPageChange={(next) => history.setQuery((prev) => ({ ...prev, page: next }))}
            />
          ) : null}
        </div>

        <div className="grid gap-5 xl:col-span-3 xl:grid-cols-3">
          <div className="xl:col-span-2">
            <ChatDetailPanel
              detail={history.selected}
              loading={history.detailLoading}
              onOpenInChatbot={(conversationId) => navigate(`/chatbot?conversationId=${conversationId}`)}
            />
          </div>
          <div className="xl:col-span-1">
            <ChatHistorySidebar summary={summary} recent={recent} />
          </div>
        </div>
      </div>
    </section>
  )
}
