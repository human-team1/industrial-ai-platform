from __future__ import annotations

from typing import Generic, TypeVar

from pydantic import BaseModel, Field, ConfigDict

from domain.rag.models import (
    AnswerType,
    QuestionMode,
    ResultContext,
    SafetyFlag,
    SourceChunkResponse,
)


T = TypeVar("T")


# API 성공 응답의 표준 래퍼.
class ApiSuccessResponse(BaseModel, Generic[T]):
    """
    프로젝트 공통 성공 응답 래퍼.
    """

    success: bool = True
    data: T
    message: str = "요청이 성공적으로 처리되었습니다."


class ProblemDetail(BaseModel):
    """
    RFC9457 Problem Details 기반 실패 응답 스키마.
    실제 전역 exception handler는 이후 단계에서 연결한다.
    """

    type: str = "about:blank"
    title: str
    status: int
    detail: str
    instance: str | None = None
    errorCode: str | None = None
    errors: list[dict[str, str]] | None = None
    requestId: str | None = None


# RAG 질의 엔드포인트 요청 계약.
class RagQueryRequest(BaseModel):
    """
    POST /api/v1/rag/query 요청 스키마.
    Spring/Frontend 계약의 기준이 된다.
    """

    model_config = ConfigDict(extra="forbid")

    user_id: int = Field(..., ge=1)
    organization_id: int = Field(..., ge=1)

    question: str = Field(..., min_length=2, max_length=1000)

    result_id: str | None = None
    result_context: ResultContext | None = None

    top_k: int = Field(default=5, ge=1, le=20)

    prompt_version: str | None = Field(
        default=None,
        description="prompt_v1_basic, prompt_v2_action_format, prompt_v3_grounded 등",
    )

    stream: bool = False
    debug: bool = False


# 성공 응답 바디에 들어갈 데이터.
class RagQueryResponseData(BaseModel):
    """
    RAG API의 data 본문.
    """

    model_config = ConfigDict(extra="ignore")

    answer: str
    answer_type: AnswerType | str
    question_mode: QuestionMode | str

    sources: list[SourceChunkResponse] = Field(default_factory=list)

    need_clarification: bool = False
    citation_ok: bool = False
    safety_flags: list[SafetyFlag | str] = Field(default_factory=list)

    trace_id: str | None = None

    # 실험/평가용 필드
    prompt_version: str | None = None
    graph_version: str = "graph_v0"
    route_path: list[str] = Field(default_factory=list)

    llm_called: bool = False
    retriever_called: bool = False
    latency_ms: int | None = None


class RagQuerySuccessResponse(ApiSuccessResponse[RagQueryResponseData]):
    pass
