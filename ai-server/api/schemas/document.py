from dataclasses import asdict
from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field

from domain.document_models import DocumentIndexCommand, DocumentIndexResult


def to_camel(value: str) -> str:
    parts = value.split("_")
    return parts[0] + "".join(part.capitalize() for part in parts[1:])


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

    def to_command(self) -> DocumentIndexCommand:
        return DocumentIndexCommand(
            document_id=self.document_id,
            document_version_id=self.document_version_id,
            file_id=self.file_id,
            file_key=self.file_key,
            document_type=self.document_type,
            organization_id=self.organization_id,
        )


class IndexedChunkResponse(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    sequence_no: int
    content: str
    page_no: Optional[int] = None
    section: Optional[str] = None
    vector_ref: str


class DocumentIndexResultResponse(BaseModel):
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
    chunks: list[IndexedChunkResponse] = Field(default_factory=list)


def document_index_result_to_response(result: DocumentIndexResult) -> DocumentIndexResultResponse:
    return DocumentIndexResultResponse(**asdict(result))
