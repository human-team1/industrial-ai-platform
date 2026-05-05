from __future__ import annotations

from enum import Enum
from typing import Any

from pydantic import BaseModel, ConfigDict, Field

class QuestionMode(str, Enum):
    # 그래프에서 사용하는 질문 라우팅 유형.
    RESULT_LINKED = "result_linked"
    DOCUMENT_SEARCH = "document_search"
    GENERAL = "general"
    OUT_OF_SCOPE = "out_of_scope"


class AnswerType(str, Enum):
    RESULT_LINKED = "result_linked"
    DOCUMENT_SEARCH = "document_search"
    GENERAL = "general"
    NO_RETRIEVAL_RESULT = "no_retrieval_result"
    OUT_OF_SCOPE = "out_of_scope"
    NEED_RESULT_CONTEXT = "need_result_context"
    VALIDATION_ERROR = "validation_error"
    SYSTEM_ERROR = "system_error"
    # Step 7 추가
    RETRIEVER_ERROR = "retriever_error"


class SafetyFlag(str, Enum):
    NO_SOURCES = "no_sources"
    OUT_OF_SCOPE = "out_of_scope"
    PROMPT_INJECTION = "prompt_injection"
    LOW_SOURCE_SCORE = "low_source_score"
    MISSING_RESULT_ID = "missing_result_id"
    RESULT_CONTEXT_NOT_FOUND = "result_context_not_found"
    VALIDATION_ERROR = "validation_error"
    RETRIEVER_ERROR = "retriever_error"
    LLM_ERROR = "llm_error"
    CITATION_MISSING = "citation_missing"
    
    # Step 12 추가
    SOURCE_METADATA_MISSING = "source_metadata_missing"
    UNSUPPORTED_CLAIM = "unsupported_claim"


class DecisionCode(str, Enum):
    NORMAL = "정상"
    DEFECT = "불량"
    REVIEW_REQUIRED = "재검사"
    UNKNOWN = "unknown"


class ResultContext(BaseModel):
    """
    결과 상세 화면 또는 result_id 기반 질문에서 주입되는 검사 결과 문맥.
    Step 9 전까지는 mock JSON에서 로드한다.
    """
    model_config = ConfigDict(extra="ignore", protected_namespaces=())

    result_id: str | None = None
    inspection_id: str | None = None

    equipment_name: str | None = None
    category: str | None = None

    decision: DecisionCode | str | None = None
    anomaly_score: float | None = None
    confidence: float | None = None

    anomaly_type: str | None = None
    heatmap_location: str | None = None

    model_version: str | None = None
    threshold_profile: str | None = None

    artifact_uri: str | None = None
    artifact_available: bool = False

    additional_status: list[str] = Field(default_factory=list)
    metadata: dict[str, Any] = Field(default_factory=dict)


class LLMGenerateResult(BaseModel):
    model_config = ConfigDict(extra="ignore", protected_namespaces=())

    # LLM 어댑터가 반환하는 최종 결과.
    answer: str
    model_name: str | None = None

    prompt_tokens: int | None = None
    completion_tokens: int | None = None
    total_tokens: int | None = None

    latency_ms: int | None = None
    raw_response: dict[str, Any] | None = None


