from dataclasses import dataclass, field
from datetime import datetime


@dataclass(frozen=True, slots=True)
class DocumentIndexCommand:
    document_id: int | None = None
    document_version_id: int | None = None
    file_id: int | None = None
    file_key: str | None = None
    document_type: str | None = None
    organization_id: int | None = None


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
