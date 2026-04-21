from fastapi import APIRouter

from api.documents import router as documents_router
from api.health import router as health_router
from api.inference import router as inference_router
from api.rag import router as rag_router

router = APIRouter()
router.include_router(health_router, tags=["health"])
router.include_router(inference_router, prefix="/inference", tags=["inference"])
router.include_router(documents_router, prefix="/documents", tags=["documents"])
router.include_router(rag_router, prefix="/rag", tags=["rag"])
