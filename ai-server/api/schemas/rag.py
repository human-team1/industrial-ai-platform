from dataclasses import asdict
from typing import Any, Generic, TypeVar

from pydantic import BaseModel, ConfigDict, Field

from domain.rag.models import AnswerType, SafetyFlag
from domain.rag.query_models import RagQueryCommand, RagQueryResult

T = TypeVar("T")


def to_camel(value: str) -> str:
    parts = value.split("_")
    return parts[0] + "".join(part.capitalize() for part in parts[1:])


class RagQueryRequest(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True, extra="forbid")

    question: str = Field(..., min_length=2, max_length=1000)
    user_id: int = Field(..., ge=1)
    organization_id: int = Field(..., ge=1)
    conversation_id: int | str | None = None
    result_id: str | None = None
    result_context: dict[str, Any] | None = None
    top_k: int = Field(default=5, ge=1, le=20)
    prompt_version: str | None = None
    stream: bool = False
    debug: bool = False

    def to_command(self) -> RagQueryCommand:
        return RagQueryCommand(
            user_id=self.user_id,
            organization_id=self.organization_id,
            question=self.question,
            result_id=self.result_id,
            result_context=self.result_context,
            top_k=self.top_k,
            prompt_version=self.prompt_version,
            stream=self.stream,
            debug=self.debug,
        )


class RagSourceResponse(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    document_id: str
    document_version_id: str | None = None
    document_title: str
    chunk_id: str
    page_no: int | None = None
    section: str | None = None
    score: float | None = None
    source_snippet: str | None = None


class RagQueryResponse(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    answer: str
    answer_status: str
    question_mode: str | None = None
    sources: list[RagSourceResponse] = Field(default_factory=list)
    llm_model: str | None = None
    created_at: str | None = None

    # Legacy/debug fields kept for /ai/v1/rag/query compatibility.
    answer_type: AnswerType | str | None = None
    citation_ok: bool = False
    need_clarification: bool = False
    safety_flags: list[SafetyFlag | str] = Field(default_factory=list)
    source_warnings: list[str] = Field(default_factory=list)
    errors: list[str] = Field(default_factory=list)
    result_id: str | None = None
    conversation_id: str | None = None
    message_id: str | None = None
    metadata: dict[str, Any] = Field(default_factory=dict)


class ApiSuccessResponse(BaseModel, Generic[T]):
    success: bool = True
    data: T
    message: str = "요청이 성공적으로 처리되었습니다."


class ProblemDetail(BaseModel):
    type: str = "about:blank"
    title: str
    status: int
    detail: str
    instance: str | None = None
    errorCode: str | None = None
    errors: list[dict[str, str]] | None = None
    requestId: str | None = None


class RagQuerySuccessResponse(ApiSuccessResponse[RagQueryResponse]):
    pass


def rag_result_to_response(result: RagQueryResult) -> RagQueryResponse:
    answer_status = str(result.metadata.get("answerStatus") or result.answer_type)
    return RagQueryResponse(
        answer=result.answer,
        answer_status=answer_status,
        question_mode=str(result.question_mode) if result.question_mode is not None else None,
        sources=[
            RagSourceResponse(
                document_id=str(source.document_id),
                document_version_id=source.document_version_id,
                document_title=source.title,
                chunk_id=str(source.chunk_id),
                page_no=source.page,
                section=source.section_title,
                score=source.score,
                source_snippet=source.source_snippet,
            )
            for source in result.sources
        ],
        llm_model=result.metadata.get("llmModel"),
        created_at=result.metadata.get("createdAt"),
        answer_type=result.answer_type,
        citation_ok=result.citation_ok,
        need_clarification=result.need_clarification,
        safety_flags=result.safety_flags,
        source_warnings=result.source_warnings,
        errors=result.errors,
        result_id=result.result_id,
        conversation_id=result.conversation_id,
        message_id=result.message_id,
        metadata={**asdict(result).get("metadata", {})},
    )
