import uuid

from fastapi import APIRouter, BackgroundTasks, Depends, Header

from api.schemas import (
    DocumentDeindexRequest,
    DocumentIndexRequest,
    deindex_result_to_response,
    enqueue_job_result_to_response,
    job_status_result_to_response,
)
from application.document_index_jobs import (
    DeindexDocumentVersionUseCase,
    EnqueueDocumentIndexJobUseCase,
    GetDocumentIndexJobStatusUseCase,
    ProcessDocumentIndexJobUseCase,
)
from container.dependencies import (
    get_deindex_document_version_usecase,
    get_document_index_job_status_usecase,
    get_enqueue_document_index_job_usecase,
    get_process_document_index_job_usecase,
)

router = APIRouter()


@router.post("/documents/index")
async def index_document_internal(
    request: DocumentIndexRequest,
    background_tasks: BackgroundTasks,
    x_request_id: str | None = Header(default=None, alias="X-Request-Id"),
    enqueue_usecase: EnqueueDocumentIndexJobUseCase = Depends(get_enqueue_document_index_job_usecase),
    process_usecase: ProcessDocumentIndexJobUseCase = Depends(get_process_document_index_job_usecase),
) -> dict:
    request_id = x_request_id or str(uuid.uuid4())
    job = request.to_job()
    result = enqueue_usecase.enqueue(job, request_id)
    background_tasks.add_task(process_usecase.process, job, request_id)
    return {
        "success": True,
        "data": enqueue_job_result_to_response(result).model_dump(by_alias=True),
        "message": "문서 인덱싱 작업이 등록되었습니다.",
    }


@router.get("/document-index-jobs/{aiJobId}")
async def get_document_index_job_status(
    aiJobId: str,
    usecase: GetDocumentIndexJobStatusUseCase = Depends(get_document_index_job_status_usecase),
) -> dict:
    result = usecase.get(aiJobId)
    message = "문서 인덱싱 작업이 실패했습니다." if result.indexing_status.value == "FAILED" else "문서 인덱싱 작업 상태를 조회했습니다."
    return {
        "success": True,
        "data": job_status_result_to_response(result).model_dump(by_alias=True),
        "message": message,
    }


@router.delete("/document-versions/{documentVersionId}/index")
async def deindex_document_version(
    documentVersionId: int,
    request: DocumentDeindexRequest,
    usecase: DeindexDocumentVersionUseCase = Depends(get_deindex_document_version_usecase),
) -> dict:
    result = usecase.deindex(request.to_command(documentVersionId))
    return {
        "success": True,
        "data": deindex_result_to_response(result).model_dump(by_alias=True),
        "message": "문서 검색 인덱스가 삭제되었습니다.",
    }
