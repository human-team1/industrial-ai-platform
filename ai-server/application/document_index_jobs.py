from __future__ import annotations

import logging
from datetime import datetime

from application.document_indexing_service import DocumentIndexingService
from application.exceptions import AppException
from domain.document_models import (
    DocumentDeindexCommand,
    DocumentDeindexResult,
    DocumentIndexCommand,
    DocumentIndexJob,
    DocumentIndexJobResult,
    DocumentIndexJobStatus,
)
from domain.document_ports import DocumentIndexJobQueuePort, DocumentIndexJobStatusPort, VectorStorePort

logger = logging.getLogger(__name__)


class EnqueueDocumentIndexJobUseCase:
    def __init__(self, queue: DocumentIndexJobQueuePort, status_store: DocumentIndexJobStatusPort) -> None:
        self._queue = queue
        self._status_store = status_store

    def enqueue(self, job: DocumentIndexJob, request_id: str) -> DocumentIndexJobResult:
        try:
            self._queue.enqueue(job)
        except Exception as exc:
            raise AppException(500, "Document index queue failed", "문서 인덱싱 작업 등록에 실패했습니다.", "DOCUMENT-JOB-500") from exc

        result = self._status_store.get_job_result(job.ai_job_id)
        if result is None:
            result = DocumentIndexJobResult(
                ai_job_id=job.ai_job_id,
                index_job_id=job.index_job_id,
                document_id=job.document_id,
                document_version_id=job.document_version_id,
                organization_id=job.organization_id,
                indexing_status=DocumentIndexJobStatus.PENDING,
                collection_name=job.collection_name,
                queued_at=job.queued_at,
            )
            self._status_store.save_job_result(result)

        logger.info(
            "document index job queued requestId=%s aiJobId=%s documentVersionId=%s organizationId=%s status=%s",
            request_id,
            job.ai_job_id,
            job.document_version_id,
            job.organization_id,
            result.indexing_status.value,
        )
        return result


class GetDocumentIndexJobStatusUseCase:
    def __init__(self, status_store: DocumentIndexJobStatusPort) -> None:
        self._status_store = status_store

    def get(self, ai_job_id: str) -> DocumentIndexJobResult:
        result = self._status_store.get_job_result(ai_job_id)
        if result is None:
            raise AppException(404, "Document index job not found", "문서 인덱싱 작업을 찾을 수 없습니다.", "DOCUMENT-JOB-404")
        return result


class ProcessDocumentIndexJobUseCase:
    def __init__(self, indexing_service: DocumentIndexingService, status_store: DocumentIndexJobStatusPort) -> None:
        self._indexing_service = indexing_service
        self._status_store = status_store

    async def process(self, job: DocumentIndexJob, request_id: str) -> None:
        started_at = datetime.now()
        self._status_store.save_job_result(
            DocumentIndexJobResult(
                ai_job_id=job.ai_job_id,
                index_job_id=job.index_job_id,
                document_id=job.document_id,
                document_version_id=job.document_version_id,
                organization_id=job.organization_id,
                indexing_status=DocumentIndexJobStatus.PROCESSING,
                collection_name=job.collection_name,
                queued_at=job.queued_at,
                started_at=started_at,
            )
        )
        try:
            result = await self._indexing_service.index_document(self._to_index_command(job), request_id)
            completed_at = datetime.now()
            self._status_store.save_job_result(
                DocumentIndexJobResult(
                    ai_job_id=job.ai_job_id,
                    index_job_id=job.index_job_id,
                    document_id=job.document_id,
                    document_version_id=job.document_version_id,
                    organization_id=job.organization_id,
                    indexing_status=DocumentIndexJobStatus.COMPLETED,
                    collection_name=result.collection_name,
                    indexed_chunk_count=len(result.chunks),
                    chunks=result.chunks,
                    queued_at=job.queued_at,
                    started_at=started_at,
                    completed_at=completed_at,
                )
            )
        except Exception as exc:
            completed_at = datetime.now()
            self._status_store.save_job_result(
                DocumentIndexJobResult(
                    ai_job_id=job.ai_job_id,
                    index_job_id=job.index_job_id,
                    document_id=job.document_id,
                    document_version_id=job.document_version_id,
                    organization_id=job.organization_id,
                    indexing_status=DocumentIndexJobStatus.FAILED,
                    collection_name=job.collection_name,
                    indexed_chunk_count=0,
                    chunks=[],
                    error_message=str(exc),
                    queued_at=job.queued_at,
                    started_at=started_at,
                    completed_at=completed_at,
                )
            )
            logger.warning(
                "document index job failed requestId=%s aiJobId=%s documentVersionId=%s organizationId=%s",
                request_id,
                job.ai_job_id,
                job.document_version_id,
                job.organization_id,
            )

    def _to_index_command(self, job: DocumentIndexJob) -> DocumentIndexCommand:
        return DocumentIndexCommand(
            document_id=job.document_id,
            document_version_id=job.document_version_id,
            file_id=job.file.file_id,
            file_key=job.file.file_key,
            document_type=self._resolve_parser_type(job),
            organization_id=job.organization_id,
            document_title=job.metadata.title,
            category=job.metadata.category,
            equipment_type=job.metadata.equipment_type,
            chunk_size=job.chunking.chunk_size,
            chunk_overlap=job.chunking.chunk_overlap,
        )

    def _resolve_parser_type(self, job: DocumentIndexJob) -> str:
        if job.file.mime_type == "application/pdf":
            return "PDF"
        if job.file.mime_type == "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            return "DOCX"
        if job.file.mime_type == "text/markdown":
            return "MD"
        return "TXT"


class DeindexDocumentVersionUseCase:
    def __init__(self, vector_store: VectorStorePort) -> None:
        self._vector_store = vector_store

    def deindex(self, command: DocumentDeindexCommand) -> DocumentDeindexResult:
        collection_name = self._vector_store.collection_name_for_organization(command.organization_id)
        try:
            deleted_count = self._vector_store.delete_document_version(collection_name, command.document_version_id)
        except Exception as exc:
            raise AppException(500, "Document deindex failed", "문서 검색 인덱스 삭제에 실패했습니다.", "DOCUMENT-DEINDEX-500") from exc
        return DocumentDeindexResult(
            document_id=command.document_id,
            document_version_id=command.document_version_id,
            organization_id=command.organization_id,
            collection_name=collection_name,
            deleted_vector_count=deleted_count,
            deindexed_at=datetime.now(),
        )
