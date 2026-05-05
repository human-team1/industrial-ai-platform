import logging
from datetime import datetime

from application.exceptions import AppException
from config.settings import Settings
from domain.document_models import DocumentIndexCommand, DocumentIndexResult, IndexedChunk, ParsedSection
from domain.document_ports import (
    DocumentParserPort,
    DocumentStatusPort,
    EmbeddingPort,
    ObjectStoragePort,
    VectorStorePort,
)

logger = logging.getLogger(__name__)


class DocumentIndexingService:
    SUPPORTED_TYPES = {"PDF", "DOCX", "MD", "TXT"}

    def __init__(
        self,
        settings: Settings,
        storage: ObjectStoragePort,
        parser: DocumentParserPort,
        embedding: EmbeddingPort,
        vector_store: VectorStorePort,
        status_store: DocumentStatusPort,
    ) -> None:
        self._settings = settings
        self._storage = storage
        self._parser = parser
        self._embedding = embedding
        self._vector_store = vector_store
        self._status_store = status_store

    async def index_document(
        self,
        request: DocumentIndexCommand,
        request_id: str,
    ) -> DocumentIndexResult:
        self._validate(request)
        document_type = str(request.document_type).upper()

        try:
            content = self._storage.download_object(
                self._storage.document_bucket_name(),
                str(request.file_key),
            )
        except Exception as exc:
            message = str(exc)
            error_code = getattr(exc, "code", None)
            if (
                error_code in {"NoSuchKey", "NoSuchObject", "NoSuchBucket", "AI_OBJECT_NOT_FOUND"}
                or "NoSuchKey" in message
                or "NoSuchObject" in message
                or "not found" in message.lower()
            ):
                self._log_failure(request, request_id, "minio_download", "file not found")
                raise AppException(404, "Document file not found", "문서 원본 파일을 찾을 수 없습니다.", "DOCUMENT-404") from exc
            self._log_failure(request, request_id, "minio_download", "download failed")
            raise AppException(500, "Document download failed", "문서 원본 다운로드에 실패했습니다.", "DOCUMENT-500") from exc

        sections = self._parser.parse(content, document_type)
        chunks = self._chunk(sections, request.document_version_id, request.chunk_size, request.chunk_overlap)
        embeddings = self._embedding.embed_texts([chunk.content for chunk in chunks])
        collection_name = (
            self._vector_store.collection_name_for_organization(request.organization_id)
            if request.organization_id is not None
            else self._vector_store.collection_name()
        )
        vector_count = self._vector_store.upsert_document_chunks(request, chunks, embeddings, collection_name)
        self._status_store.set_status(str(request.document_version_id), "COMPLETED")

        logger.info(
            "document indexing completed requestId=%s documentId=%s documentVersionId=%s fileKey=%s chunkCount=%s",
            request_id,
            request.document_id,
            request.document_version_id,
            request.file_key,
            len(chunks),
        )
        return DocumentIndexResult(
            document_id=request.document_id,
            document_version_id=request.document_version_id,
            file_id=request.file_id,
            file_key=str(request.file_key),
            indexing_status="COMPLETED",
            chunk_count=len(chunks),
            vector_count=vector_count,
            embedding_model=self._settings.rag_embedding_model_name,
            collection_name=collection_name,
            indexed_at=datetime.now(),
            chunks=chunks,
        )

    def _validate(self, request: DocumentIndexCommand) -> None:
        if request.document_id is None or request.document_version_id is None or request.file_id is None:
            raise AppException(400, "Invalid request", "documentId, documentVersionId, fileId는 필수입니다.", "DOCUMENT-400")
        if request.file_key is None or not request.file_key.strip():
            raise AppException(400, "Invalid request", "fileKey는 필수입니다.", "DOCUMENT-400")
        if request.document_type is None or request.document_type.upper() not in self.SUPPORTED_TYPES:
            raise AppException(422, "Unsupported document type", "지원하지 않는 documentType입니다.", "DOCUMENT-422")

    def _chunk(
        self,
        sections: list[ParsedSection],
        document_version_id: int,
        request_chunk_size: int | None = None,
        request_chunk_overlap: int | None = None,
    ) -> list[IndexedChunk]:
        chunk_size = request_chunk_size or self._settings.rag_chunk_size
        overlap = min(
            request_chunk_overlap if request_chunk_overlap is not None else self._settings.rag_chunk_overlap,
            max(chunk_size - 1, 0),
        )
        chunks: list[IndexedChunk] = []
        sequence_no = 1
        for section in sections:
            start = 0
            text = section.content
            while start < len(text):
                end = min(start + chunk_size, len(text))
                chunk_text = text[start:end].strip()
                if chunk_text:
                    chunks.append(
                        IndexedChunk(
                            sequence_no=sequence_no,
                            content=chunk_text,
                            page_no=section.page_no,
                            section=section.section,
                            vector_ref=f"docver-{document_version_id}-chunk-{sequence_no}",
                        )
                    )
                    sequence_no += 1
                if end >= len(text):
                    break
                start = max(end - overlap, start + 1)
        return chunks

    def _log_failure(
        self,
        request: DocumentIndexCommand,
        request_id: str,
        stage: str,
        error: str,
    ) -> None:
        logger.warning(
            "document indexing failed requestId=%s documentId=%s documentVersionId=%s fileKey=%s stage=%s error=%s",
            request_id,
            request.document_id,
            request.document_version_id,
            request.file_key,
            stage,
            error,
        )
