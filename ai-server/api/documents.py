from fastapi import APIRouter, Depends, File, UploadFile

from application.document_indexing_service import DocumentIndexingService
from container.dependencies import get_document_indexing_service
from domain.schemas import DocumentIndexResponse

router = APIRouter()


@router.post("/index", response_model=DocumentIndexResponse)
async def index_document(
    file: UploadFile = File(...),
    service: DocumentIndexingService = Depends(get_document_indexing_service),
) -> DocumentIndexResponse:
    content = await file.read()
    return await service.index_document(file.filename or "uploaded-document", content)
