from pydantic import BaseModel, Field, model_validator


class SourceChunkMetadata(BaseModel):
    organization_id: str = Field(default="org-001")
    document_status: str = Field(default="PUBLISHED")
    document_version_policy: str = Field(default="latest_only")

    chunk_size: int = Field(default=800)
    chunk_overlap: int = Field(default=100)

    embedding_model: str = Field(default="BAAI/bge-m3")
    vector_ref: str | None = None

    source_group: str | None = None
    relative_path: str | None = None
    created_at: str | None = None


class SourceChunk(BaseModel):
    chunk_id: str = Field(min_length=1)
    document_id: str = Field(min_length=1)
    document_version_id: str = Field(min_length=1)

    title: str = Field(min_length=1)
    document_type: str | None = None

    category: str | None = None
    equipment_name: str | None = None

    section_title: str | None = None
    page: int | None = None

    content: str

    score: float | None = None
    rank: int | None = None

    source_uri: str | None = None
    metadata: SourceChunkMetadata | None = None

    @model_validator(mode="after")
    def validate_source_locator(self) -> "SourceChunk":
        if self.section_title is None and self.page is None:
            raise ValueError("section_title 또는 page 중 최소 하나는 필요합니다.")

        return self
