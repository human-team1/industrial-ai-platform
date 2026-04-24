from fastapi import APIRouter
from fastapi import Depends

from container.dependencies import (
    get_chroma_client,
    get_minio_storage,
    get_redis_status_store,
)
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.minio_storage import MinioStorage
from infrastructure.redis_status import RedisStatusStore

router = APIRouter()


@router.get("/health")
async def health() -> dict[str, str]:
    return {"status": "UP", "service": "ai-server"}


@router.get("/health/infra")
async def infra_health(
    chroma_client: ChromaClientWrapper = Depends(get_chroma_client),
    minio_storage: MinioStorage = Depends(get_minio_storage),
    redis_status_store: RedisStatusStore = Depends(get_redis_status_store),
) -> dict[str, str]:
    return {
        "redis": "UP" if redis_status_store.ping() else "DOWN",
        "minio": "UP" if minio_storage.is_document_bucket_accessible() else "DOWN",
        "chromadb": "UP" if chroma_client.health_check() else "DOWN",
    }
