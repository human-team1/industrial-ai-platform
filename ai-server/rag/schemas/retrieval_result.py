from pydantic import BaseModel, Field

from rag.schemas.source_chunk import SourceChunk


class RetrievalFilters(BaseModel):
    organization_id: str = Field(default="org-001")
    document_status: str = Field(default="PUBLISHED")

    document_version_policy: str = Field(default="latest_only")

    equipment_name: str | None = None
    document_type: str | None = None
    category: str | None = None
    source_group: str | None = None


class RetrievalQuery(BaseModel):
    query: str = Field(min_length=2, max_length=1000)
    filters: RetrievalFilters = Field(default_factory=RetrievalFilters)
    top_k: int = Field(default=5, ge=1, le=20)
    query_case_id: str | None = None
    request_id: str | None = None


class RetrievalResult(BaseModel):
    query: str
    sources: list[SourceChunk]
    retrieval_config_id: str

    latency_ms: float | None = None
    total_candidates: int | None = None
    request_id: str | None = None
    query_case_id: str | None = None
