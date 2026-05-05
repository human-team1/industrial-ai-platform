from dataclasses import asdict
from typing import Any, Generic, TypeVar

from pydantic import BaseModel, ConfigDict, Field

from domain.rag.models import AnswerType, QuestionMode, ResultContext, SafetyFlag
from domain.rag.query_models import RagQueryCommand, RagQueryResult

T = TypeVar("T")


class RagQueryRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    user_id: int = Field(..., ge=1)
    organization_id: int = Field(..., ge=1)
    question: str = Field(..., min_length=2, max_length=1000)
    result_id: str | None = None
    result_context: ResultContext | None = None
    top_k: int = Field(default=5, ge=1, le=20)
    prompt_version: str | None = Field(default=None, description="prompt version override")
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
    document_id: str
    document_version_id: str | None = None
    chunk_id: str
    title: str
    document_type: str | None = None
    section_title: str | None = None
    page: int | None = None
    score: float | None = None
    source_snippet: str | None = None
    source_uri: str | None = None


class RagQueryResponse(BaseModel):
    answer: str
    answer_type: AnswerType | str
    question_mode: QuestionMode | str | None = None
    citation_ok: bool = False
    sources: list[RagSourceResponse] = Field(default_factory=list)
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
    return RagQueryResponse(**asdict(result))
