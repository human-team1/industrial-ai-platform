from dataclasses import dataclass, field
from datetime import datetime
from enum import Enum


@dataclass(frozen=True, slots=True)
class DocumentIndexCommand:
    document_id: int | None = None
    document_version_id: int | None = None
    file_id: int | None = None
    file_key: str | None = None
    document_type: str | None = None
    organization_id: int | None = None
    document_title: str | None = None
    category: str | None = None
    equipment_type: str | None = None
    chunk_size: int | None = None
    chunk_overlap: int | None = None


@dataclass(frozen=True, slots=True)
class ParsedSection:
    content: str
    page_no: int | None = None
    section: str | None = None


@dataclass(frozen=True, slots=True)
class IndexedChunk:
    sequence_no: int
    content: str
    page_no: int | None = None
    section: str | None = None
    vector_ref: str = ""


@dataclass(frozen=True, slots=True)
class DocumentIndexResult:
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
    chunks: list[IndexedChunk] = field(default_factory=list)


class DocumentIndexJobStatus(str, Enum):
    PENDING = "PENDING"
    PROCESSING = "PROCESSING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"


@dataclass(frozen=True, slots=True)
class DocumentFileRef:
    file_id: int | None
    file_key: str
    file_name: str | None = None
    mime_type: str | None = None
    checksum: str | None = None


@dataclass(frozen=True, slots=True)
class DocumentMetadata:
    title: str | None = None
    document_type: str | None = None
    category: str | None = None
    equipment_type: str | None = None
    tags: list[str] = field(default_factory=list)


@dataclass(frozen=True, slots=True)
class DocumentChunkingOptions:
    chunk_size: int
    chunk_overlap: int


@dataclass(frozen=True, slots=True)
class DocumentEmbeddingOptions:
    embedding_model: str = "BAAI/bge-m3"


@dataclass(frozen=True, slots=True)
class DocumentIndexJob:
    ai_job_id: str
    index_job_id: int
    document_id: int
    document_version_id: int
    organization_id: int
    file: DocumentFileRef
    metadata: DocumentMetadata
    chunking: DocumentChunkingOptions
    embedding: DocumentEmbeddingOptions
    collection_name: str
    status: DocumentIndexJobStatus = DocumentIndexJobStatus.PENDING
    queued_at: datetime | None = None


@dataclass(frozen=True, slots=True)
class DocumentIndexJobResult:
    ai_job_id: str
    index_job_id: int
    document_id: int
    document_version_id: int
    organization_id: int
    indexing_status: DocumentIndexJobStatus
    collection_name: str
    embedding_model: str | None = None
    indexed_chunk_count: int = 0
    chunks: list[IndexedChunk] = field(default_factory=list)
    error_message: str | None = None
    queued_at: datetime | None = None
    started_at: datetime | None = None
    completed_at: datetime | None = None


@dataclass(frozen=True, slots=True)
class DocumentDeindexCommand:
    document_version_id: int
    organization_id: int
    document_id: int | None = None
    reason: str | None = None


@dataclass(frozen=True, slots=True)
class DocumentDeindexResult:
    document_version_id: int
    organization_id: int
    collection_name: str
    deleted_vector_count: int
    deindexed_at: datetime
    document_id: int | None = None
