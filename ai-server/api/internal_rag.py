from fastapi import APIRouter, Depends

from api.schemas import RagQueryRequest, rag_result_to_response
from application.rag_service import RagService
from container.dependencies import get_rag_service

router = APIRouter()


@router.post("/rag/query")
async def query_rag_internal(
    request: RagQueryRequest,
    service: RagService = Depends(get_rag_service),
) -> dict:
    response = rag_result_to_response(service.query(request.to_command()))
    return {
        "success": True,
        "data": response.model_dump(by_alias=True),
        "message": "문서 기반 답변이 생성되었습니다.",
    }
