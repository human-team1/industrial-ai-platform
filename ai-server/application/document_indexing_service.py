from domain.schemas import DocumentIndexResponse
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.minio_storage import MinioStorage
from infrastructure.redis_status import RedisStatusStore


class DocumentIndexingService:
    def __init__(
        self,
        chroma_client: ChromaClientWrapper,
        storage: MinioStorage,
        status_store: RedisStatusStore,
    ) -> None:
        self._chroma_client = chroma_client
        self._storage = storage
        self._status_store = status_store

    async def index_document(self, filename: str, content: bytes) -> DocumentIndexResponse:
        document_id = filename
        self._storage.put_object(document_id, content)
        self._status_store.set_status(document_id, "INDEXING_SKIPPED")
        self._chroma_client.collection_name()
        return DocumentIndexResponse(document_id=document_id, status="ACCEPTED")
