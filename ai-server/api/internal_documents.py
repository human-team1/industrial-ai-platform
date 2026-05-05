import uuid

from fastapi import APIRouter, Depends, Header

from application.document_indexing_service import DocumentIndexingService
from container.dependencies import get_document_indexing_service
from domain.schemas import DocumentIndexRequest

router = APIRouter()


@router.post("/documents/index")
async def index_document_internal(
    request: DocumentIndexRequest,
    x_request_id: str | None = Header(default=None, alias="X-Request-Id"),
    service: DocumentIndexingService = Depends(get_document_indexing_service),
) -> dict:
    request_id = x_request_id or str(uuid.uuid4())
    result = await service.index_document(request, request_id)
    return {
        "success": True,
        "data": result.model_dump(by_alias=True),
        "message": "문서 인덱싱이 완료되었습니다.",
    }
