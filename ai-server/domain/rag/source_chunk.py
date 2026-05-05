from __future__ import annotations

from typing import Any

from pydantic import AliasChoices, BaseModel, ConfigDict, Field, model_validator


class SourceChunk(BaseModel):
    """
    Retriever가 반환하는 검색 결과 chunk.

    A/B 통합 기준:
    - A는 검색 결과 sources를 책임진다.
    - B는 SourceChunk를 받아 prompt/source_verifier/API 응답에 사용한다.

    호환성:
    - A 기준 필드: document_id, section_title
    - 기존 B mock 호환 필드: doc_id, section
    """

    model_config = ConfigDict(
        extra="ignore",
        populate_by_name=True,
    )

    chunk_id: str | int
    document_id: str | int = Field(
        validation_alias=AliasChoices("document_id", "doc_id")
    )
    document_version_id: str | int

    title: str
    content: str

    document_type: str | None = None
    category: str | None = None
    equipment_name: str | None = None

    section_title: str | None = Field(
        default=None,
        validation_alias=AliasChoices("section_title", "section"),
    )
    page: int | None = None

    score: float
    rank: int | None = None

    source_uri: str | None = None
    source_type: str = "document"

    metadata: dict[str, Any] = Field(default_factory=dict)

    @model_validator(mode="after")
    def validate_source_location(self):
        """
        출처 식별을 위해 section_title 또는 page 중 최소 1개는 있어야 한다.
        """
        if self.section_title is None and self.page is None:
            raise ValueError("section_title or page is required for SourceChunk")

        return self

    @property
    def doc_id(self) -> str | int:
        return self.document_id

    @property
    def section(self) -> str | None:
        return self.section_title


class SourceChunkResponse(BaseModel):
    """
    API 응답용 source.

    content 전체는 응답에 직접 노출하지 않고,
    출처 식별 정보와 snippet 중심으로 반환한다.
    """

    model_config = ConfigDict(
        extra="ignore",
        populate_by_name=True,
    )

    chunk_id: str | int
    document_id: str | int = Field(
        validation_alias=AliasChoices("document_id", "doc_id")
    )
    document_version_id: str | int | None = None

    title: str

    document_type: str | None = None
    category: str | None = None
    equipment_name: str | None = None

    section_title: str | None = Field(
        default=None,
        validation_alias=AliasChoices("section_title", "section"),
    )
    page: int | None = None

    score: float | None = None
    rank: int | None = None

    source_uri: str | None = None
    source_snippet: str | None = None

    @property
    def doc_id(self) -> str | int:
        return self.document_id

    @property
    def section(self) -> str | None:
        return self.section_title