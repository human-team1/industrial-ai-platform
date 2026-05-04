from __future__ import annotations

from typing import Any

from pydantic import BaseModel, Field

from domain.rag.models import AnswerType, QuestionMode, SafetyFlag


class RagSourceResponse(BaseModel):
    """
    API 응답용 source.

    내부 SourceChunk의 content 전문은 노출하지 않고,
    source_snippet 중심으로 내려준다.
    """

    document_id: str
    document_version_id: str | None = None
    chunk_id: str

    title: str
    document_type: str | None = None
    section_title: str | None = None
    page: int | None = None

    score: float | None = None
    source_snippet: str | None = None

    # MinIO/public URL 정책 확정 전까지 optional.
    # 로컬 rag/corpus 경로는 mapper에서 제거한다.
    source_uri: str | None = None


class RagQueryResponse(BaseModel):
    """
    FastAPI 성공 응답의 data payload.

    실제 endpoint 응답은 Step 16 이후:
    {
      "success": true,
      "data": RagQueryResponse,
      "message": "챗봇 답변이 생성되었습니다."
    }
    형태로 감싼다.
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

    # 대화 저장 구조 연결 전까지 optional.
    conversation_id: str | None = None
    message_id: str | None = None

    # Step 13~15 검증용 metadata.
    # 운영 API에서는 route_path, prompt_version 등 일부 축소 가능.
    metadata: dict[str, Any] = Field(default_factory=dict)