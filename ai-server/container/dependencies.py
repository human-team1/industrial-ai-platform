from functools import lru_cache

from application.anomaly_service import AnomalyService
from application.document_indexing_service import DocumentIndexingService
from application.document_index_jobs import (
    DeindexDocumentVersionUseCase,
    EnqueueDocumentIndexJobUseCase,
    GetDocumentIndexJobStatusUseCase,
    ProcessDocumentIndexJobUseCase,
)
from application.rag.graph_runner import RagGraphRunner
from application.rag_service import RagService
from application.vision_inference_service import VisionInferenceService
from container.rag_container import create_rag_graph_runner
from config.settings import get_settings
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.concurrency.inference_limiter import InferenceLimiter
from infrastructure.document_parser import DocumentParser
from infrastructure.document_job_store import RedisDocumentIndexJobStore
from infrastructure.embedding_client import EmbeddingClient
from infrastructure.heatmap_generator import HeatmapGenerator
from infrastructure.image_preprocessor import VisionImagePreprocessor
from infrastructure.llm.ollama_llm_client import OllamaLLMClient
from infrastructure.memory_bank_loader import MemoryBankLoader
from infrastructure.minio_storage import MinioStorage
from infrastructure.quality_evaluator import QualityEvaluator
from infrastructure.redis_status import RedisStatusStore
from infrastructure.retriever.chroma_retriever import ChromaRetriever
from infrastructure.vision_config_loader import VisionConfigLoader
from infrastructure.vision_model_loader import VisionModelLoader
from infrastructure.vision.anomalib_patchcore_inferencer import AnomalibPatchcoreInferencer


@lru_cache
def get_chroma_client() -> ChromaClientWrapper:
    return ChromaClientWrapper(get_settings())


@lru_cache
def get_minio_storage() -> MinioStorage:
    return MinioStorage(get_settings())


@lru_cache
def get_redis_status_store() -> RedisStatusStore:
    return RedisStatusStore(get_settings())


@lru_cache
def get_document_index_job_store() -> RedisDocumentIndexJobStore:
    return RedisDocumentIndexJobStore(get_settings())


def get_anomaly_service() -> AnomalyService:
    return AnomalyService(get_settings())


def get_document_indexing_service() -> DocumentIndexingService:
    return DocumentIndexingService(
        get_settings(),
        get_minio_storage(),
        DocumentParser(),
        EmbeddingClient(get_settings()),
        get_chroma_client(),
        get_redis_status_store(),
    )


def get_rag_service() -> RagService:
    return RagService(
        settings=get_settings(),
        retriever=get_chroma_retriever(),
        llm_client=get_ollama_llm_client(),
    )


@lru_cache
def get_rag_graph_runner() -> RagGraphRunner:
    return create_rag_graph_runner(get_settings())


def get_enqueue_document_index_job_usecase() -> EnqueueDocumentIndexJobUseCase:
    store = get_document_index_job_store()
    return EnqueueDocumentIndexJobUseCase(store, store)


def get_document_index_job_status_usecase() -> GetDocumentIndexJobStatusUseCase:
    return GetDocumentIndexJobStatusUseCase(get_document_index_job_store())


def get_process_document_index_job_usecase() -> ProcessDocumentIndexJobUseCase:
    return ProcessDocumentIndexJobUseCase(
        get_document_indexing_service(),
        get_document_index_job_store(),
    )


def get_deindex_document_version_usecase() -> DeindexDocumentVersionUseCase:
    return DeindexDocumentVersionUseCase(get_chroma_client())


@lru_cache
def get_chroma_retriever() -> ChromaRetriever:
    settings = get_settings()
    return ChromaRetriever(
        settings=settings,
        chroma_client=get_chroma_client(),
        embedding_client=EmbeddingClient(settings),
    )


@lru_cache
def get_ollama_llm_client() -> OllamaLLMClient:
    settings = get_settings()
    return OllamaLLMClient(
        base_url=settings.ollama_base_url,
        model_name=settings.llm_model_name,
        temperature=settings.llm_temperature,
        max_tokens=settings.llm_max_tokens,
        timeout_seconds=settings.llm_timeout_seconds,
    )


@lru_cache
def get_vision_config_loader() -> VisionConfigLoader:
    return VisionConfigLoader()


@lru_cache
def get_vision_model_loader() -> VisionModelLoader:
    return VisionModelLoader()


@lru_cache
def get_memory_bank_loader() -> MemoryBankLoader:
    return MemoryBankLoader()


@lru_cache
def get_quality_evaluator() -> QualityEvaluator:
    return QualityEvaluator()


@lru_cache
def get_vision_preprocessor() -> VisionImagePreprocessor:
    return VisionImagePreprocessor()


@lru_cache
def get_anomalib_inferencer() -> AnomalibPatchcoreInferencer:
    return AnomalibPatchcoreInferencer()


@lru_cache
def get_heatmap_generator() -> HeatmapGenerator:
    return HeatmapGenerator()


@lru_cache
def get_inference_limiter() -> InferenceLimiter:
    settings = get_settings()
    return InferenceLimiter(
        max_concurrency=settings.max_inference_concurrency,
        queue_size=settings.inference_queue_size,
    )


@lru_cache
def get_vision_inference_service() -> VisionInferenceService:
    settings = get_settings()
    return VisionInferenceService(
        storage=get_minio_storage(),
        config_loader=get_vision_config_loader(),
        model_loader=get_vision_model_loader(),
        memory_bank_loader=get_memory_bank_loader(),
        preprocessor=get_vision_preprocessor(),
        quality_evaluator=get_quality_evaluator(),
        inferencer=get_anomalib_inferencer(),
        heatmap_generator=get_heatmap_generator(),
        inference_limiter=get_inference_limiter(),
        inspection_artifact_bucket_name=settings.minio_bucket_inspection_artifacts,
        model_bucket_name=settings.minio_bucket_models,
    )
