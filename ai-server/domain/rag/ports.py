from __future__ import annotations

from typing import Any, Protocol

from domain.rag.models import (
    LLMGenerateResult,
    ResultContext,
    RetrievalResult,
    SourceChunk,
)
from domain.rag.state import GraphState


# 그래프에서 사용하는 Retriever 포트.
class RetrieverPort(Protocol):
    """
    MockRetriever와 ChromaRetriever가 구현할 검색 포트.
    Step 8에서는 MockRetriever가 먼저 구현하고,
    Step 15에서 ChromaRetriever로 교체한다.
    """

    async def search(
        self,
        *,
        query: str,
        organization_id: int,
        top_k: int,
        filters: dict[str, Any] | None = None,
    ) -> RetrievalResult:
        ...


# 생성 단계에서 사용하는 LLM 포트.
class LLMPort(Protocol):
    """
    OpenAI, Gemini, Claude, Local LLM 등으로 교체 가능한 LLM 포트.
    Step 11에서 OpenAI 구현체를 먼저 붙인다.
    """

    async def generate(
        self,
        *,
        prompt: str,
        model_name: str | None = None,
        temperature: float | None = None,
        max_tokens: int | None = None,
        metadata: dict[str, Any] | None = None,
    ) -> LLMGenerateResult:
        ...


# 결과 연동 질문에서 사용하는 결과 문맥 조회 포트.
class ResultContextPort(Protocol):
    """
    result_id 기반 검사 결과 문맥 조회 포트.
    이번 실험에서는 mock JSON 구현체를 사용하고,
    정식 연동 시 Spring 또는 MariaDB 조회로 교체한다.
    """

    async def get_result_context(
        self,
        *,
        result_id: str,
        user_id: int,
        organization_id: int,
    ) -> ResultContext | None:
        ...


# 대화와 실행 추적 저장 포트.
class ConversationStorePort(Protocol):
    """
    대화/그래프 실행 로그 저장 포트.
    이번 실험에서는 JSONL 저장,
    정식 서비스에서는 CHAT_CONVERSATION, CHAT_MESSAGE, CHAT_SOURCE 저장으로 교체한다.
    """

    async def save_graph_run(
        self,
        *,
        state: GraphState,
    ) -> None:
        ...

    async def save_conversation(
        self,
        *,
        user_id: int,
        organization_id: int,
        question: str,
        answer: str,
        sources: list[SourceChunk],
        trace_id: str | None = None,
    ) -> None:
        ...


# LangSmith 메타데이터와 trace_id 제공 포트.
class TracePort(Protocol):
    """
    LangSmith 추적 보조 포트.
    Step 3에서 구현한다.
    """

    def build_metadata(
        self,
        *,
        state: GraphState,
        extra: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        ...

    def get_trace_id(self) -> str | None:
        ...
