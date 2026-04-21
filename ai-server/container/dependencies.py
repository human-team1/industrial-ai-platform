from functools import lru_cache

from application.anomaly_service import AnomalyService
from application.document_indexing_service import DocumentIndexingService
from application.rag_service import RagService
from config.settings import get_settings
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.minio_storage import MinioStorage
from infrastructure.redis_status import RedisStatusStore


@lru_cache
def get_chroma_client() -> ChromaClientWrapper:
    return ChromaClientWrapper(get_settings())


@lru_cache
def get_minio_storage() -> MinioStorage:
    return MinioStorage(get_settings())


@lru_cache
def get_redis_status_store() -> RedisStatusStore:
    return RedisStatusStore(get_settings())


def get_anomaly_service() -> AnomalyService:
    return AnomalyService(get_settings())


def get_document_indexing_service() -> DocumentIndexingService:
    return DocumentIndexingService(
        get_chroma_client(),
        get_minio_storage(),
        get_redis_status_store(),
    )


def get_rag_service() -> RagService:
    return RagService(get_chroma_client())
