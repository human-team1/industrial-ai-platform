import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useChatbot } from '../../features/chatbot/model'
import { ChatInput, ChatMessageList, RecommendedQuestions } from '../../features/chatbot/ui'

export function ChatbotWidget({ initialConversationId }: { initialConversationId?: number | null }) {
  const chatbot = useChatbot(initialConversationId)
  const [inputDraft, setInputDraft] = useState('')
  const pending = chatbot.sendStatus === 'pending'

  return (
    <section className="space-y-5">
      <div className="page-panel flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">챗봇</h1>
          <p className="mt-2 text-sm text-slate-600">
            업로드된 문서와 지식베이스를 기반으로 답변을 제공합니다. (답변 생성은 별도 AI 서버 연동 후 가능합니다.)
          </p>
        </div>
        <div className="flex gap-2">
          <button className="btn-secondary" type="button" onClick={chatbot.startNew}>
            새 대화
          </button>
          <Link className="btn-secondary inline-flex items-center" to="/chatbot/history">
            히스토리
          </Link>
        </div>
      </div>

      {chatbot.error ? (
        chatbot.sendStatus === 'failed' ? (
          <div className="rounded-md border border-red-200 bg-red-50 p-4 text-sm text-red-800">
            <p>{chatbot.error}</p>
            <div className="mt-3 flex flex-wrap gap-2">
              <button
                className="btn-primary"
                type="button"
                disabled={pending}
                onClick={() => void chatbot.retryLastQuestion()}
              >
                재시도
              </button>
            </div>
          </div>
        ) : (
          <div className="rounded-md border border-red-200 bg-red-50 p-4 text-sm text-red-800">{chatbot.error}</div>
        )
      ) : null}

      <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_320px]">
        <div className="page-panel space-y-4">
          <ChatMessageList
            messages={chatbot.messages}
            detailLoading={chatbot.detailLoading}
            sendStatus={chatbot.sendStatus}
          />
          <ChatInput
            sendStatus={chatbot.sendStatus}
            documentScope={chatbot.documentScope}
            onScopeChange={chatbot.setDocumentScope}
            value={inputDraft}
            onChange={setInputDraft}
            onSubmit={(q) => {
              void chatbot.sendQuestion(q)
              setInputDraft('')
            }}
          />
          <p className="text-xs text-slate-500">
            AI가 생성한 답변은 부정확할 수 있습니다. 중요한 의사결정은 반드시 원본 문서를 확인하세요.
          </p>
        </div>

        <aside className="page-panel space-y-3">
          <h2 className="text-sm font-semibold text-slate-900">추천 질문 (MVP 정적)</h2>
          <p className="text-xs text-slate-500">API 미연동 · 아래 문장은 입력 보조용입니다.</p>
          <RecommendedQuestions
            disabled={pending}
            onSend={(q) => void chatbot.sendQuestion(q)}
            onFill={(q) => setInputDraft(q)}
          />
        </aside>
      </div>
    </section>
  )
}
