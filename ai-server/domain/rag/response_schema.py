from __future__ import annotations

from typing import Any

from pydantic import BaseModel, Field

from domain.rag.models import AnswerType, QuestionMode, SafetyFlag


class RagSourceResponse(BaseModel):
    """RAG 응답의 source payload."""

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
    """
    FastAPI 성공 응답의 data payload.

    실제 endpoint 응답은 다음 형태로 감싼다.
    {
      "success": true,
      "data": RagQueryResponse,
      "message": "챗봇 답변이 생성되었습니다."
    }
    """

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
