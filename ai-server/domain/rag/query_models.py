from dataclasses import dataclass, field
from typing import Any

from domain.rag.models import AnswerType, QuestionMode, SafetyFlag


@dataclass(frozen=True, slots=True)
class RagQueryCommand:
    user_id: int
    organization_id: int
    question: str
    result_id: int | str | None = None
    result_context: dict[str, Any] | None = None
    top_k: int = 5
    prompt_version: str | None = None
    stream: bool = False
    debug: bool = False


@dataclass(frozen=True, slots=True)
class RagSourceResult:
    document_id: str
    chunk_id: str
    document_version_id: str | None = None
    title: str = "제목 없음"
    document_type: str | None = None
    section_title: str | None = None
    page: int | None = None
    score: float | None = None
    source_snippet: str | None = None
    source_uri: str | None = None


@dataclass(frozen=True, slots=True)
class RagQueryResult:
    answer: str
    answer_type: AnswerType | str
    question_mode: QuestionMode | str | None = None
    citation_ok: bool = False
    sources: list[RagSourceResult] = field(default_factory=list)
    need_clarification: bool = False
    safety_flags: list[SafetyFlag | str] = field(default_factory=list)
    source_warnings: list[str] = field(default_factory=list)
    errors: list[str] = field(default_factory=list)
    result_id: int | str | None = None
    conversation_id: str | None = None
    message_id: str | None = None
    metadata: dict[str, Any] = field(default_factory=dict)
