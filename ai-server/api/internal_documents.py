import uuid

from fastapi import APIRouter, Depends, Header

from application.document_indexing_service import DocumentIndexingService
from api.schemas import DocumentIndexRequest, document_index_result_to_response
from container.dependencies import get_document_indexing_service

router = APIRouter()


@router.post("/documents/index")
async def index_document_internal(
    request: DocumentIndexRequest,
    x_request_id: str | None = Header(default=None, alias="X-Request-Id"),
    service: DocumentIndexingService = Depends(get_document_indexing_service),
) -> dict:
    request_id = x_request_id or str(uuid.uuid4())
    result = await service.index_document(request.to_command(), request_id)
    response = document_index_result_to_response(result)
    return {
        "success": True,
        "data": response.model_dump(by_alias=True),
        "message": "문서 인덱싱이 완료되었습니다.",
    }
