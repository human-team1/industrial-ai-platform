from typing import Literal

from pydantic import BaseModel, Field


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
    retrieval_config_id: str = Field(default="R3_HYBRID_TK10_S04")

    collection_name: str = Field(default="industrial_rag_chunks_a_v1")
    embedding_model: str = Field(default="BAAI/bge-m3")

    chunk_size: int = Field(default=800)
    chunk_overlap: int = Field(default=100)

    search_mode: SearchMode = Field(default="hybrid")

    top_k: int = Field(default=10)
    internal_top_k: int = Field(default=10)
    answer_top_k: int = Field(default=5)
    visible_source_limit: int = Field(default=3)

    min_score: float | None = Field(default=0.4)

    organization_filter_required: bool = Field(default=True)
    document_status_filter: str = Field(default="PUBLISHED")
    document_version_policy: DocumentVersionPolicy = Field(default="latest_only")

    deduplicate_by_document: bool = Field(default=True)
    diversify_by_section: bool = Field(default=True)
    mmr_enabled: bool = Field(default=False)
    reranker_enabled: bool = Field(default=False)

    return_fields: list[str] = Field(
        default_factory=lambda: [
            "chunk_id",
            "document_id",
            "document_version_id",
            "title",
            "document_type",
            "category",
            "equipment_name",
            "section_title",
            "page",
            "content",
            "score",
            "rank",
            "source_uri",
            "metadata",
        ]
    )
