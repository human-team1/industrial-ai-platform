from fastapi import APIRouter, Depends, File, UploadFile

from application.document_indexing_service import DocumentIndexingService
from container.dependencies import get_document_indexing_service
from domain.schemas import LegacyDocumentIndexResponse

router = APIRouter()


@router.post("/index", response_model=LegacyDocumentIndexResponse)
async def index_document(
    file: UploadFile = File(...),
    service: DocumentIndexingService = Depends(get_document_indexing_service),
) -> LegacyDocumentIndexResponse:
    content = await file.read()
    _ = content
    return LegacyDocumentIndexResponse(document_id=file.filename or "uploaded-document", status="ACCEPTED")
