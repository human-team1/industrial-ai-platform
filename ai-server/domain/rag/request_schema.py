from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from domain.rag.models import ResultContext


class RagQueryRequest(BaseModel):
    """POST /api/v1/rag/query 요청 스키마."""

    model_config = ConfigDict(extra="forbid")

    user_id: int = Field(..., ge=1)
    organization_id: int = Field(..., ge=1)
    question: str = Field(..., min_length=2, max_length=1000)

    result_id: str | None = None
    result_context: ResultContext | None = None

    top_k: int = Field(default=5, ge=1, le=20)
    prompt_version: str | None = Field(
        default=None,
        description="prompt version override",
    )
    stream: bool = False
    debug: bool = False
