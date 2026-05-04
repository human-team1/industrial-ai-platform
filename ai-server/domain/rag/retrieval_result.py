from __future__ import annotations

from typing import Any

from pydantic import AliasChoices, BaseModel, ConfigDict, Field, field_validator

from domain.rag.source_chunk import SourceChunk


class RetrievalQuery(BaseModel):
    """
    Retriever 호출 입력값.

    A ChromaRetriever / MockRetriever / HTTP Retriever가 공통으로 받을 수 있는 검색 요청 계약.
    """

    model_config = ConfigDict(
        extra="ignore",
        populate_by_name=True,
    )

    query: str
    top_k: int = Field(default=5, ge=1, le=20)
    filters: dict[str, Any] = Field(
        default_factory=dict,
        validation_alias=AliasChoices("filters", "filter"),
    )

    @field_validator("query")
    @classmethod
    def validate_query(cls, value: str) -> str:
        value = value.strip()

        if not value:
            raise ValueError("query must not be empty")

        return value


class RetrievalResult(BaseModel):
    """
    Retriever가 반환하는 정규화된 검색 결과.
    """

    model_config = ConfigDict(extra="ignore")

    query: str
    sources: list[SourceChunk] = Field(default_factory=list)

    retriever_type: str = "mock"
    retrieval_config_id: str | None = None

    latency_ms: int | None = None
    error_message: str | None = None

    @property
    def source_count(self) -> int:
        return len(self.sources)