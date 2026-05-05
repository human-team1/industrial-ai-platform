from __future__ import annotations

import json
from dataclasses import asdict
from datetime import datetime

from config.settings import Settings
from domain.document_models import (
    DocumentIndexJob,
    DocumentIndexJobResult,
    DocumentIndexJobStatus,
    IndexedChunk,
)


class RedisDocumentIndexJobStore:
    _memory_results: dict[str, DocumentIndexJobResult] = {}
    _memory_queue: list[str] = []

    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    def enqueue(self, job: DocumentIndexJob) -> None:
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
        self.save_job_result(result)
        if self._settings.redis_enabled:
            client = self._client()
            client.rpush("document-index-jobs", self._job_to_json(job))
            return
        self._memory_queue.append(job.ai_job_id)

    def save_job_result(self, result: DocumentIndexJobResult) -> None:
        if self._settings.redis_enabled:
            self._client().set(
                self._status_key(result.ai_job_id),
                self._result_to_json(result),
                ex=self._settings.redis_ttl_seconds,
            )
            return
        self._memory_results[result.ai_job_id] = result

    def get_job_result(self, ai_job_id: str) -> DocumentIndexJobResult | None:
        if self._settings.redis_enabled:
            raw = self._client().get(self._status_key(ai_job_id))
            if raw is None:
                return None
            return self._result_from_json(str(raw))
        return self._memory_results.get(ai_job_id)

    def _client(self):
        import redis

        return redis.Redis(
            host=self._settings.redis_host,
            port=self._settings.redis_port,
            password=self._settings.redis_password or None,
            db=self._settings.redis_db,
            decode_responses=True,
        )

    def _status_key(self, ai_job_id: str) -> str:
        return f"document-index-job:{ai_job_id}:status"

    def _job_to_json(self, job: DocumentIndexJob) -> str:
        return json.dumps(self._normalize(asdict(job)), ensure_ascii=False)

    def _result_to_json(self, result: DocumentIndexJobResult) -> str:
        return json.dumps(self._normalize(asdict(result)), ensure_ascii=False)

    def _result_from_json(self, raw: str) -> DocumentIndexJobResult:
        data = json.loads(raw)
        chunks = [IndexedChunk(**chunk) for chunk in data.get("chunks", [])]
        return DocumentIndexJobResult(
            ai_job_id=data["ai_job_id"],
            index_job_id=data["index_job_id"],
            document_id=data["document_id"],
            document_version_id=data["document_version_id"],
            organization_id=data["organization_id"],
            indexing_status=DocumentIndexJobStatus(data["indexing_status"]),
            collection_name=data["collection_name"],
            indexed_chunk_count=data.get("indexed_chunk_count", 0),
            chunks=chunks,
            error_message=data.get("error_message"),
            queued_at=self._parse_datetime(data.get("queued_at")),
            started_at=self._parse_datetime(data.get("started_at")),
            completed_at=self._parse_datetime(data.get("completed_at")),
        )

    def _normalize(self, value):
        if isinstance(value, dict):
            return {key: self._normalize(item) for key, item in value.items()}
        if isinstance(value, list):
            return [self._normalize(item) for item in value]
        if isinstance(value, datetime):
            return value.isoformat()
        if isinstance(value, DocumentIndexJobStatus):
            return value.value
        return value

    def _parse_datetime(self, value: str | None) -> datetime | None:
        if not value:
            return None
        return datetime.fromisoformat(value)
