from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field


def to_camel(value: str) -> str:
    parts = value.split("_")
    return parts[0] + "".join(part.capitalize() for part in parts[1:])


class AnomalyInferenceRequest(BaseModel):
    equipment_id: str = Field(..., examples=["press-01"])
    sensor_values: dict[str, float] = Field(default_factory=dict)


class AnomalyInferenceResponse(BaseModel):
    model_config = ConfigDict(protected_namespaces=())

    equipment_id: str
    is_anomaly: bool
    score: float
    model_name: str


class LegacyDocumentIndexResponse(BaseModel):
    document_id: str
    status: str


class DocumentIndexRequest(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    document_id: Optional[int] = None
    document_version_id: Optional[int] = None
    file_id: Optional[int] = None
    file_key: Optional[str] = None
    document_type: Optional[str] = None
    organization_id: Optional[int] = None


class IndexedChunk(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    sequence_no: int
    content: str
    page_no: Optional[int] = None
    section: Optional[str] = None
    vector_ref: str


class DocumentIndexResult(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    document_id: int
    document_version_id: int
    file_id: int
    file_key: str
    indexing_status: str
    chunk_count: int
    vector_count: int
    embedding_model: str
    collection_name: str
    indexed_at: datetime
    chunks: list[IndexedChunk] = Field(default_factory=list)


class RagQueryRequest(BaseModel):
    question: str
    top_k: int = Field(default=3, ge=1, le=10)


class RagQueryResponse(BaseModel):
    answer: str
    sources: list[str] = Field(default_factory=list)
