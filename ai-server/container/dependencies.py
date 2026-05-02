from functools import lru_cache

from application.anomaly_service import AnomalyService
from application.document_indexing_service import DocumentIndexingService
from application.rag_graph import InspectionRagGraph
from application.rag_service import RagService
from config.settings import get_settings
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.embedding_client import EmbeddingClient
from infrastructure.minio_storage import MinioStorage
from infrastructure.redis_status import RedisStatusStore
from rag.retrievers import ChromaRetriever, RetrieverPort


@lru_cache
def get_chroma_client() -> ChromaClientWrapper:
    return ChromaClientWrapper(get_settings())


@lru_cache
def get_embedding_client() -> EmbeddingClient:
    return EmbeddingClient(get_settings())


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
        get_settings(),
        get_chroma_client(),
        get_embedding_client(),
        get_minio_storage(),
        get_redis_status_store(),
    )


@lru_cache
def get_retriever() -> RetrieverPort:
    return ChromaRetriever(
        get_settings(),
        get_chroma_client(),
        get_embedding_client(),
    )


@lru_cache
def get_rag_graph() -> InspectionRagGraph:
    return InspectionRagGraph(get_settings(), get_retriever())


def get_rag_service() -> RagService:
    return RagService(get_rag_graph())
