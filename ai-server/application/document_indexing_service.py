import hashlib
import io
import logging
import re
from dataclasses import dataclass
from datetime import datetime
from typing import Optional

from application.exceptions import AppException
from config.settings import Settings
from domain.schemas import DocumentIndexRequest, DocumentIndexResult, IndexedChunk
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.minio_storage import MinioStorage
from infrastructure.redis_status import RedisStatusStore

logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class ParsedSection:
    content: str
    page_no: Optional[int] = None
    section: Optional[str] = None


class DocumentIndexingService:
    SUPPORTED_TYPES = {"PDF", "DOCX", "MD", "TXT"}

    def __init__(
        self,
        settings: Settings,
        chroma_client: ChromaClientWrapper,
        storage: MinioStorage,
        status_store: RedisStatusStore,
    ) -> None:
        self._settings = settings
        self._chroma_client = chroma_client
        self._storage = storage
        self._status_store = status_store

    async def index_document(
        self,
        request: DocumentIndexRequest,
        request_id: str,
    ) -> DocumentIndexResult:
        self._validate(request)
        document_type = request.document_type.upper()

        try:
            content = self._storage.download_object(
                self._storage.document_bucket_name(),
                request.file_key,
            )
        except Exception as exc:
            message = str(exc)
            error_code = getattr(exc, "code", None)
            if (
                error_code in {"NoSuchKey", "NoSuchObject", "NoSuchBucket"}
                or "NoSuchKey" in message
                or "NoSuchObject" in message
                or "not found" in message.lower()
            ):
                self._log_failure(request, request_id, "minio_download", "file not found")
                raise AppException(404, "Document file not found", "문서 원본 파일을 찾을 수 없습니다.", "DOCUMENT-404")
            self._log_failure(request, request_id, "minio_download", "download failed")
            raise AppException(500, "Document download failed", "문서 원본 다운로드에 실패했습니다.", "DOCUMENT-500")

        sections = self._parse(content, document_type)
        chunks = self._chunk(sections, request.document_version_id)
        embeddings = [self._embed(chunk.content) for chunk in chunks]
        vector_count = self._upsert_vectors(request, chunks, embeddings)
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
            file_key=request.file_key,
            indexing_status="COMPLETED",
            chunk_count=len(chunks),
            vector_count=vector_count,
            embedding_model=self._settings.rag_embedding_model_name,
            collection_name=self._settings.chroma_collection_name,
            indexed_at=datetime.now(),
            chunks=chunks,
        )

    def _validate(self, request: DocumentIndexRequest) -> None:
        if request.document_id is None or request.document_version_id is None or request.file_id is None:
            raise AppException(400, "Invalid request", "documentId, documentVersionId, fileId는 필수입니다.", "DOCUMENT-400")
        if request.file_key is None or not request.file_key.strip():
            raise AppException(400, "Invalid request", "fileKey는 필수입니다.", "DOCUMENT-400")
        if request.document_type is None or request.document_type.upper() not in self.SUPPORTED_TYPES:
            raise AppException(422, "Unsupported document type", "지원하지 않는 documentType입니다.", "DOCUMENT-422")

    def _parse(self, content: bytes, document_type: str) -> list[ParsedSection]:
        if document_type == "PDF":
            return self._parse_pdf(content)
        if document_type == "DOCX":
            return self._parse_docx(content)
        text = self._decode_text(content)
        return [ParsedSection(content=text)]

    def _parse_pdf(self, content: bytes) -> list[ParsedSection]:
        from pypdf import PdfReader

        try:
            reader = PdfReader(io.BytesIO(content))
            sections = [
                ParsedSection(content=page.extract_text() or "", page_no=index + 1)
                for index, page in enumerate(reader.pages)
            ]
        except Exception:
            raise AppException(422, "Document parse failed", "PDF 문서 파싱에 실패했습니다.", "DOCUMENT-422")
        return self._preprocess(sections)

    def _parse_docx(self, content: bytes) -> list[ParsedSection]:
        from docx import Document

        try:
            document = Document(io.BytesIO(content))
            text = "\n".join(paragraph.text for paragraph in document.paragraphs)
        except Exception:
            raise AppException(422, "Document parse failed", "DOCX 문서 파싱에 실패했습니다.", "DOCUMENT-422")
        return self._preprocess([ParsedSection(content=text)])

    def _decode_text(self, content: bytes) -> str:
        try:
            return content.decode("utf-8")
        except UnicodeDecodeError:
            return content.decode("cp949", errors="ignore")

    def _preprocess(self, sections: list[ParsedSection]) -> list[ParsedSection]:
        cleaned: list[ParsedSection] = []
        for section in sections:
            text = re.sub(r"\s+", " ", section.content).strip()
            if len(text) < 10:
                continue
            cleaned.append(ParsedSection(content=text, page_no=section.page_no, section=section.section))
        if not cleaned:
            raise AppException(422, "Empty document", "인덱싱할 수 있는 문서 텍스트가 없습니다.", "DOCUMENT-422")
        return cleaned

    def _chunk(self, sections: list[ParsedSection], document_version_id: int) -> list[IndexedChunk]:
        chunk_size = self._settings.rag_chunk_size
        overlap = min(self._settings.rag_chunk_overlap, max(chunk_size - 1, 0))
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

    def _embed(self, content: str) -> list[float]:
        digest = hashlib.sha256(content.encode("utf-8")).digest()
        values = [((byte / 255.0) * 2.0) - 1.0 for byte in digest]
        return values

    def _upsert_vectors(
        self,
        request: DocumentIndexRequest,
        chunks: list[IndexedChunk],
        embeddings: list[list[float]],
    ) -> int:
        collection = self._chroma_client.get_or_create_document_collection()
        existing_ids = [chunk.vector_ref for chunk in chunks]
        try:
            collection.delete(where={"documentVersionId": request.document_version_id})
        except Exception:
            if existing_ids:
                try:
                    collection.delete(ids=existing_ids)
                except Exception:
                    pass
        metadatas = []
        for chunk in chunks:
            metadata = {
                "documentId": request.document_id,
                "documentVersionId": request.document_version_id,
                "fileId": request.file_id,
                "fileKey": request.file_key,
                "documentType": request.document_type.upper(),
                "sequenceNo": chunk.sequence_no,
            }
            if chunk.page_no is not None:
                metadata["pageNo"] = chunk.page_no
            if chunk.section:
                metadata["section"] = chunk.section
            metadatas.append(metadata)
        collection.add(
            ids=existing_ids,
            documents=[chunk.content for chunk in chunks],
            embeddings=embeddings,
            metadatas=metadatas,
        )
        return len(chunks)

    def _log_failure(
        self,
        request: DocumentIndexRequest,
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
