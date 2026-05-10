from __future__ import annotations

from typing import Any

from pydantic import AliasChoices, BaseModel, ConfigDict, Field, model_validator


class SourceChunk(BaseModel):
    """
    Retriever가 반환하는 검색 결과 chunk.

    문서 형식별 메타데이터 편차를 허용하기 위해
    조회 단계에서는 page/section이 비어 있어도 통과시킨다.
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
    def normalize_source_location(self):
        if self.section_title == "":
            self.section_title = None
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
