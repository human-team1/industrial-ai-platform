import socket
import time
from datetime import datetime
from pathlib import Path
from shutil import disk_usage

from fastapi import APIRouter, Request
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


@router.get("/internal/system-status")
async def internal_system_status(request: Request) -> dict[str, object]:
    started = time.perf_counter()
    checked_at = datetime.now().replace(microsecond=0).isoformat()
    disk = disk_usage(Path.cwd().anchor or ".")
    disk_usage_percent = round(((disk.total - disk.free) / disk.total) * 100, 2) if disk.total else None
    return {
        "success": True,
        "data": {
            "nodeType": "AI_SERVER",
            "nodeName": "AI 모델 서버",
            "hostName": socket.gethostname(),
            "instanceId": "ai-server-local",
            "status": "NORMAL",
            "cpuUsage": None,
            "memoryUsage": None,
            "diskUsage": disk_usage_percent,
            "responseTimeMs": int((time.perf_counter() - started) * 1000),
            "message": "AI 서버 상태 조회 정상",
            "checkedAt": checked_at,
            "requestId": request.headers.get("X-Request-Id"),
        },
        "message": "AI 서버 상태를 조회했습니다.",
    }
