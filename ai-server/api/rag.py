from fastapi import APIRouter, Depends

from application.rag_service import RagService
from api.schemas import RagQueryRequest, RagQueryResponse, rag_result_to_response
from container.dependencies import get_rag_service

router = APIRouter()


@router.post("/query", response_model=RagQueryResponse)
async def query_rag(
    request: RagQueryRequest,
    service: RagService = Depends(get_rag_service),
) -> RagQueryResponse:
    return rag_result_to_response(service.query(request.to_command()))
