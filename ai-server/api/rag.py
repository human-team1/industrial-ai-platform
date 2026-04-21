from fastapi import APIRouter, Depends

from application.rag_service import RagService
from container.dependencies import get_rag_service
from domain.schemas import RagQueryRequest, RagQueryResponse

router = APIRouter()


@router.post("/query", response_model=RagQueryResponse)
async def query_rag(
    request: RagQueryRequest,
    service: RagService = Depends(get_rag_service),
) -> RagQueryResponse:
    return service.query(request)
