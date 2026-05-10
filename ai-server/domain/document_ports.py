from typing import Protocol

from domain.document_models import DocumentIndexCommand, DocumentIndexJob, DocumentIndexJobResult, IndexedChunk, ParsedSection


class ObjectStoragePort(Protocol):
    def document_bucket_name(self) -> str: ...

    def download_object(self, bucket_name: str, object_name: str) -> bytes: ...


class DocumentParserPort(Protocol):
    def parse(self, content: bytes, document_type: str) -> list[ParsedSection]: ...


class EmbeddingPort(Protocol):
    def embed_texts(self, texts: list[str]) -> list[list[float]]: ...


class VectorStorePort(Protocol):
    def collection_name(self) -> str: ...

    def collection_name_for_organization(self, organization_id: int) -> str: ...

    def upsert_document_chunks(
        self,
        request: DocumentIndexCommand,
        chunks: list[IndexedChunk],
        embeddings: list[list[float]],
        collection_name: str | None = None,
    ) -> int: ...

    def delete_document_version(self, collection_name: str, document_version_id: int) -> int: ...


class DocumentStatusPort(Protocol):
    def set_status(self, key: str, status: str) -> None: ...


class DocumentIndexJobQueuePort(Protocol):
    def enqueue(self, job: DocumentIndexJob) -> None: ...


class DocumentIndexJobStatusPort(Protocol):
    def save_job_result(self, result: DocumentIndexJobResult) -> None: ...

    def get_job_result(self, ai_job_id: str) -> DocumentIndexJobResult | None: ...
