from typing import Protocol

from domain.document_models import DocumentIndexCommand, IndexedChunk, ParsedSection


class ObjectStoragePort(Protocol):
    def document_bucket_name(self) -> str: ...

    def download_object(self, bucket_name: str, object_name: str) -> bytes: ...


class DocumentParserPort(Protocol):
    def parse(self, content: bytes, document_type: str) -> list[ParsedSection]: ...


class EmbeddingPort(Protocol):
    def embed_texts(self, texts: list[str]) -> list[list[float]]: ...


class VectorStorePort(Protocol):
    def collection_name(self) -> str: ...

    def upsert_document_chunks(
        self,
        request: DocumentIndexCommand,
        chunks: list[IndexedChunk],
        embeddings: list[list[float]],
    ) -> int: ...


class DocumentStatusPort(Protocol):
    def set_status(self, key: str, status: str) -> None: ...
