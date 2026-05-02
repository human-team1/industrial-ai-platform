import logging

from config.settings import Settings
from domain.schemas import DocumentIndexResponse
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.embedding_client import EmbeddingClient
from infrastructure.minio_storage import MinioStorage
from infrastructure.redis_status import RedisStatusStore
from rag.chunking.section_chunker import ChunkRecord, SectionChunker
from rag.loaders.markdown_loader import LoadedDocument


logger = logging.getLogger(__name__)


class DocumentIndexingService:
    def __init__(
        self,
        settings: Settings,
        chroma_client: ChromaClientWrapper,
        embedding_client: EmbeddingClient,
        storage: MinioStorage,
        status_store: RedisStatusStore,
    ) -> None:
        self._settings = settings
        self._chroma_client = chroma_client
        self._embedding_client = embedding_client
        self._storage = storage
        self._status_store = status_store

    async def index_document(self, filename: str, content: bytes) -> DocumentIndexResponse:
        document_id = filename
        self._storage.put_object(document_id, content)
        self._set_status(document_id, "INDEXING")

        try:
            document = self._build_loaded_document(filename, content)
            chunks = SectionChunker(
                chunk_size=self._settings.rag_chunk_size,
                chunk_overlap=self._settings.rag_chunk_overlap,
            ).chunk_document(document)

            if not chunks:
                self._set_status(document_id, "INDEXING_FAILED")
                return DocumentIndexResponse(document_id=document_id, status="FAILED")

            texts = [chunk.content for chunk in chunks]
            embeddings = self._embedding_client.embed_texts(texts)
            collection = self._chroma_client.get_or_create_document_collection()
            collection.upsert(
                ids=[chunk.chunk_id for chunk in chunks],
                documents=texts,
                metadatas=[self._build_metadata(chunk) for chunk in chunks],
                embeddings=embeddings,
            )
            self._set_status(document_id, "INDEXED")
            return DocumentIndexResponse(document_id=document_id, status="INDEXED")
        except Exception:
            logger.exception(
                "document_indexing_failed",
                extra={"document_id": document_id, "filename": filename},
            )
            self._set_status(document_id, "INDEXING_FAILED")
            raise

    def _build_loaded_document(self, filename: str, content: bytes) -> LoadedDocument:
        text = content.decode("utf-8", errors="ignore")
        title = self._detect_title(text, filename)
        return LoadedDocument(
            relative_path=filename,
            source_group="uploaded",
            file_name=filename,
            title=title,
            document_type="UPLOADED",
            content=text,
            source_uri=f"minio://{self._storage.document_bucket_name()}/{filename}",
        )

    def _detect_title(self, text: str, filename: str) -> str:
        for line in text.splitlines():
            stripped = line.strip()
            if stripped.startswith("# "):
                return stripped.replace("# ", "", 1).strip()
        return filename

    def _build_metadata(self, chunk: ChunkRecord) -> dict:
        return {
            "chunk_id": chunk.chunk_id,
            "document_id": chunk.document_id,
            "document_version_id": chunk.document_version_id,
            "title": chunk.title,
            "document_type": chunk.document_type,
            "source_group": chunk.source_group,
            "relative_path": chunk.relative_path,
            "section_title": chunk.section_title or "",
            "section_level": chunk.section_level or "",
            "sequence_no": chunk.sequence_no,
            "char_count": chunk.char_count,
            "source_uri": chunk.source_uri,
            "organization_id": self._normalize_organization_id(
                self._settings.rag_default_organization_id
            ),
            "document_status": self._settings.rag_default_document_status,
            "document_version_policy": "latest_only",
            "retrieval_config_id": self._settings.retrieval_config_id,
            "retrieval_frozen_at": self._settings.retrieval_frozen_at,
            "chunk_size": self._settings.rag_chunk_size,
            "chunk_overlap": self._settings.rag_chunk_overlap,
            "embedding_provider": self._settings.embedding_provider,
            "embedding_model": self._settings.embedding_model_name,
        }

    def _normalize_organization_id(self, organization_id: int | str) -> str:
        if str(organization_id) == "1":
            return "org-001"
        return str(organization_id)

    def _set_status(self, document_id: str, status: str) -> None:
        try:
            self._status_store.set_status(document_id, status)
        except Exception:
            logger.warning(
                "document_indexing_status_update_failed",
                extra={"document_id": document_id, "status": status},
                exc_info=True,
            )
