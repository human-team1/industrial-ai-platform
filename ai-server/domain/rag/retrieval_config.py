from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field, field_validator, model_validator


SearchMode = Literal[
    "vector",
    "bm25",
    "hybrid",
    "vector_with_metadata_filter",
    "vector_metadata_filter",
    "hybrid_with_reranker",
    "query_rewrite_vector",
]


DocumentVersionPolicy = Literal[
    "latest_only",
    "all_versions",
]


class RetrievalConfig(BaseModel):
    """
    A/B 통합용 retrieval frozen config.

    R6 기준:
    - collection: industrial_rag_chunks_a_v1
    - embedding: text-embedding-3-small
    - search mode: vector_with_metadata_filter
    """

    retrieval_config_id: str = Field(default="R6")

    collection_name: str = Field(default="industrial_rag_chunks_a_v1")
    embedding_model: str = Field(default="text-embedding-3-small")

    chunk_size: int = Field(default=800, ge=100, le=3000)
    chunk_overlap: int = Field(default=100, ge=0)

    search_mode: SearchMode = Field(default="vector_with_metadata_filter")

    top_k: int = Field(default=5, ge=1, le=20)
    internal_top_k: int = Field(default=10, ge=1, le=50)
    answer_top_k: int = Field(default=5, ge=1, le=20)
    visible_source_limit: int = Field(default=5, ge=1, le=20)

    min_score: float | None = Field(default=0.35, ge=0.0, le=1.0)

    organization_filter_required: bool = Field(default=True)
    document_status_filter: str = Field(default="PUBLISHED")
    document_version_policy: DocumentVersionPolicy = Field(default="latest_only")

    deduplicate_by_document: bool = Field(default=True)
    diversify_by_section: bool = Field(default=True)

    return_fields: list[str] = Field(
        default_factory=lambda: [
            "chunk_id",
            "document_id",
            "document_version_id",
            "title",
            "section_title",
            "page",
            "content",
            "score",
            "rank",
        ]
    )

    @field_validator("retrieval_config_id", "collection_name", "embedding_model")
    @classmethod
    def validate_required_text(cls, value: str) -> str:
        value = value.strip()
        if not value:
            raise ValueError("required text field must not be empty")
        return value

    @field_validator("document_status_filter")
    @classmethod
    def validate_document_status_filter(cls, value: str) -> str:
        value = value.strip().upper()
        if not value:
            raise ValueError("document_status_filter must not be empty")
        return value

    @model_validator(mode="after")
    def validate_top_k_relationship(self):
        if self.internal_top_k < self.top_k:
            raise ValueError("internal_top_k must be greater than or equal to top_k")

        if self.answer_top_k > self.top_k:
            raise ValueError("answer_top_k must be less than or equal to top_k")

        if self.visible_source_limit > self.top_k:
            raise ValueError("visible_source_limit must be less than or equal to top_k")

        if self.chunk_overlap >= self.chunk_size:
            raise ValueError("chunk_overlap must be smaller than chunk_size")

        return self