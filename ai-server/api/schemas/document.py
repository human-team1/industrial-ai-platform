from dataclasses import asdict
from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field, model_validator

from application.exceptions import AppException
from domain.document_models import (
    DocumentChunkingOptions,
    DocumentDeindexCommand,
    DocumentDeindexResult,
    DocumentEmbeddingOptions,
    DocumentFileRef,
    DocumentIndexCommand,
    DocumentIndexJob,
    DocumentIndexJobResult,
    DocumentIndexJobStatus,
    DocumentIndexResult,
    DocumentMetadata,
)

SUPPORTED_MIME_TYPES = {
    "application/pdf",
    "text/plain",
    "text/markdown",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
}


def to_camel(value: str) -> str:
    parts = value.split("_")
    return parts[0] + "".join(part.capitalize() for part in parts[1:])


def collection_name_for_organization(organization_id: int) -> str:
    return f"documents_org_{organization_id}"


class LegacyDocumentIndexResponse(BaseModel):
    document_id: str
    status: str


class DocumentIndexFileRequest(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    file_id: int | None = None
    file_key: str | None = None
    file_name: str | None = None
    mime_type: str | None = None
    checksum: str | None = None


class DocumentIndexMetadataRequest(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    title: str | None = None
    document_type: str | None = None
    category: str | None = None
    equipment_type: str | None = None
    tags: list[str] = Field(default_factory=list)


class DocumentIndexChunkingRequest(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    chunk_size: int = 800
    chunk_overlap: int = 120


class DocumentIndexEmbeddingRequest(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    embedding_model: str = "default"


class DocumentIndexRequest(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    index_job_id: int | None = None
    document_id: Optional[int] = None
    document_version_id: Optional[int] = None
    organization_id: Optional[int] = None
    file: DocumentIndexFileRequest | None = None
    metadata: DocumentIndexMetadataRequest = Field(default_factory=DocumentIndexMetadataRequest)
    chunking: DocumentIndexChunkingRequest = Field(default_factory=DocumentIndexChunkingRequest)
    embedding: DocumentIndexEmbeddingRequest = Field(default_factory=DocumentIndexEmbeddingRequest)

    # Legacy flat fields kept for existing smoke tests and old callers.
    file_id: Optional[int] = None
    file_key: Optional[str] = None
    document_type: Optional[str] = None

    @model_validator(mode="after")
    def validate_index_request(self) -> "DocumentIndexRequest":
        if self.index_job_id is None:
            raise AppException(400, "Invalid request", "indexJobId는 필수입니다.", "DOCUMENT-JOB-400")
        if self.document_id is None:
            raise AppException(400, "Invalid request", "documentId는 필수입니다.", "DOCUMENT-JOB-400")
        if self.document_version_id is None:
            raise AppException(400, "Invalid request", "documentVersionId는 필수입니다.", "DOCUMENT-JOB-400")
        if self.organization_id is None:
            raise AppException(400, "Invalid request", "organizationId는 필수입니다.", "DOCUMENT-JOB-400")
        file_key = self.resolved_file_key()
        if not file_key:
            raise AppException(400, "Invalid request", "file.fileKey는 필수입니다.", "DOCUMENT-JOB-400")
        mime_type = self.resolved_mime_type()
        if mime_type not in SUPPORTED_MIME_TYPES:
            raise AppException(422, "Unsupported document MIME type", "지원하지 않는 문서 MIME 타입입니다.", "DOCUMENT-JOB-422")
        if self.chunking.chunk_size <= 0:
            raise AppException(422, "Invalid chunking", "chunkSize는 0보다 커야 합니다.", "DOCUMENT-JOB-422")
        if self.chunking.chunk_overlap < 0:
            raise AppException(422, "Invalid chunking", "chunkOverlap은 0 이상이어야 합니다.", "DOCUMENT-JOB-422")
        if self.chunking.chunk_overlap >= self.chunking.chunk_size:
            raise AppException(422, "Invalid chunking", "chunkOverlap은 chunkSize보다 작아야 합니다.", "DOCUMENT-JOB-422")
        return self

    def resolved_file_key(self) -> str | None:
        return (self.file.file_key if self.file else None) or self.file_key

    def resolved_file_id(self) -> int | None:
        return (self.file.file_id if self.file else None) or self.file_id

    def resolved_mime_type(self) -> str | None:
        if self.file and self.file.mime_type:
            return self.file.mime_type
        doc_type = (self.metadata.document_type or self.document_type or "").upper()
        return {
            "PDF": "application/pdf",
            "TXT": "text/plain",
            "MD": "text/markdown",
            "DOCX": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        }.get(doc_type)

    def to_command(self) -> DocumentIndexCommand:
        return DocumentIndexCommand(
            document_id=self.document_id,
            document_version_id=self.document_version_id,
            file_id=self.resolved_file_id(),
            file_key=self.resolved_file_key(),
            document_type=self.metadata.document_type or self.document_type,
            organization_id=self.organization_id,
            document_title=self.metadata.title,
            category=self.metadata.category,
            equipment_type=self.metadata.equipment_type,
            chunk_size=self.chunking.chunk_size,
            chunk_overlap=self.chunking.chunk_overlap,
        )

    def to_job(self) -> DocumentIndexJob:
        ai_job_id = f"doc-index-{self.index_job_id}"
        organization_id = int(self.organization_id)
        return DocumentIndexJob(
            ai_job_id=ai_job_id,
            index_job_id=int(self.index_job_id),
            document_id=int(self.document_id),
            document_version_id=int(self.document_version_id),
            organization_id=organization_id,
            file=DocumentFileRef(
                file_id=self.resolved_file_id(),
                file_key=str(self.resolved_file_key()),
                file_name=self.file.file_name if self.file else None,
                mime_type=self.resolved_mime_type(),
                checksum=self.file.checksum if self.file else None,
            ),
            metadata=DocumentMetadata(
                title=self.metadata.title,
                document_type=self.metadata.document_type or self.document_type,
                category=self.metadata.category,
                equipment_type=self.metadata.equipment_type,
                tags=self.metadata.tags,
            ),
            chunking=DocumentChunkingOptions(
                chunk_size=self.chunking.chunk_size,
                chunk_overlap=self.chunking.chunk_overlap,
            ),
            embedding=DocumentEmbeddingOptions(embedding_model=self.embedding.embedding_model),
            collection_name=collection_name_for_organization(organization_id),
            status=DocumentIndexJobStatus.PENDING,
            queued_at=datetime.now(),
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
    indexed_chunk_count: int
    chunk_count: int
    vector_count: int
    embedding_model: str
    collection_name: str
    indexed_at: datetime
    chunks: list[IndexedChunkResponse] = Field(default_factory=list)


class EnqueueDocumentIndexJobResponse(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    ai_job_id: str
    index_job_id: int
    document_id: int
    document_version_id: int
    organization_id: int
    indexing_status: str
    collection_name: str
    queued_at: datetime | None = None


class DocumentIndexJobStatusResponse(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    ai_job_id: str
    index_job_id: int
    document_id: int
    document_version_id: int
    organization_id: int
    indexing_status: str
    collection_name: str
    indexed_chunk_count: int
    chunks: list[IndexedChunkResponse] = Field(default_factory=list)
    error_message: str | None = None
    started_at: datetime | None = None
    completed_at: datetime | None = None
    queued_at: datetime | None = None


class DocumentDeindexRequest(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    organization_id: int | None = None
    document_id: int | None = None
    reason: str | None = None

    @model_validator(mode="after")
    def validate_deindex_request(self) -> "DocumentDeindexRequest":
        if self.organization_id is None:
            raise AppException(400, "Invalid request", "organizationId는 필수입니다.", "DOCUMENT-DEINDEX-400")
        return self

    def to_command(self, document_version_id: int) -> DocumentDeindexCommand:
        return DocumentDeindexCommand(
            document_version_id=document_version_id,
            organization_id=int(self.organization_id),
            document_id=self.document_id,
            reason=self.reason,
        )


class DocumentDeindexResponse(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    document_id: int | None = None
    document_version_id: int
    organization_id: int
    collection_name: str
    deleted_vector_count: int
    deindexed_at: datetime


def document_index_result_to_response(result: DocumentIndexResult) -> DocumentIndexResultResponse:
    data = asdict(result)
    data["indexed_chunk_count"] = result.chunk_count
    return DocumentIndexResultResponse(**data)


def enqueue_job_result_to_response(result: DocumentIndexJobResult) -> EnqueueDocumentIndexJobResponse:
    return EnqueueDocumentIndexJobResponse(
        ai_job_id=result.ai_job_id,
        index_job_id=result.index_job_id,
        document_id=result.document_id,
        document_version_id=result.document_version_id,
        organization_id=result.organization_id,
        indexing_status=result.indexing_status.value,
        collection_name=result.collection_name,
        queued_at=result.queued_at,
    )


def job_status_result_to_response(result: DocumentIndexJobResult) -> DocumentIndexJobStatusResponse:
    return DocumentIndexJobStatusResponse(
        ai_job_id=result.ai_job_id,
        index_job_id=result.index_job_id,
        document_id=result.document_id,
        document_version_id=result.document_version_id,
        organization_id=result.organization_id,
        indexing_status=result.indexing_status.value,
        collection_name=result.collection_name,
        indexed_chunk_count=result.indexed_chunk_count,
        chunks=[IndexedChunkResponse(**asdict(chunk)) for chunk in result.chunks],
        error_message=result.error_message,
        queued_at=result.queued_at,
        started_at=result.started_at,
        completed_at=result.completed_at,
    )


def deindex_result_to_response(result: DocumentDeindexResult) -> DocumentDeindexResponse:
    return DocumentDeindexResponse(**asdict(result))
