from functools import lru_cache

from application.anomaly_service import AnomalyService
from application.document_indexing_service import DocumentIndexingService
from application.vision_inference_service import VisionInferenceService
from config.settings import get_settings
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.concurrency.inference_limiter import InferenceLimiter
from infrastructure.embedding_client import EmbeddingClient
from infrastructure.fallback_inferencer import StatisticalFallbackInferencer
from infrastructure.heatmap_generator import HeatmapGenerator
from infrastructure.image_preprocessor import VisionImagePreprocessor
from infrastructure.memory_bank_loader import MemoryBankLoader
from infrastructure.minio_storage import MinioStorage
from infrastructure.quality_evaluator import QualityEvaluator
from infrastructure.redis_status import RedisStatusStore
from infrastructure.vision_config_loader import VisionConfigLoader
from infrastructure.vision_model_loader import VisionModelLoader


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
        get_settings(),
        get_chroma_client(),
        EmbeddingClient(get_settings()),
        get_minio_storage(),
        get_redis_status_store(),
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
def get_fallback_inferencer() -> StatisticalFallbackInferencer:
    return StatisticalFallbackInferencer()


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
        inferencer=get_fallback_inferencer(),
        heatmap_generator=get_heatmap_generator(),
        inference_limiter=get_inference_limiter(),
        inspection_artifact_bucket_name=settings.minio_bucket_inspection_artifacts,
        model_bucket_name=settings.minio_bucket_models,
    )
