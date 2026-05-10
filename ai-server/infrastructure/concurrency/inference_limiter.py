from __future__ import annotations

import asyncio
import logging
from contextlib import asynccontextmanager

from application.exceptions import AppException

logger = logging.getLogger(__name__)


class InferenceLimiter:
    def __init__(self, max_concurrency: int, queue_size: int) -> None:
        self._max_concurrency = max(1, max_concurrency)
        self._queue_size = max(1, queue_size)
        self._semaphore = asyncio.Semaphore(self._max_concurrency)
        self._state_lock = asyncio.Lock()
        self._active_count = 0
        self._reserved_count = 0

    @asynccontextmanager
    async def limit(self, request_id: str, inspection_id: int | None, model_version_id: int | None):
        async with self._state_lock:
            inflight_limit = self._max_concurrency + self._queue_size
            if self._reserved_count >= inflight_limit:
                logger.warning(
                    "[AI_INFERENCE_REJECTED] requestId=%s inspectionId=%s modelVersionId=%s activeCount=%s waitingCount=%s queueSize=%s",
                    request_id,
                    inspection_id,
                    model_version_id,
                    self._active_count,
                    max(0, self._reserved_count - self._active_count),
                    self._queue_size,
                )
                raise AppException(
                    429,
                    "Inference queue is full",
                    "AI 추론 대기열이 가득 찼습니다. 잠시 후 다시 시도해주세요.",
                    "INFERENCE_QUEUE_FULL",
                )

            queued = self._active_count >= self._max_concurrency
            self._reserved_count += 1
            waiting_count = max(0, self._reserved_count - self._active_count - self._max_concurrency)

        if queued:
            logger.info(
                "[AI_INFERENCE_WAIT] requestId=%s inspectionId=%s modelVersionId=%s waitingCount=%s",
                request_id,
                inspection_id,
                model_version_id,
                waiting_count + 1,
            )

        await self._semaphore.acquire()

        try:
            async with self._state_lock:
                self._active_count += 1
                active_count = self._active_count

            logger.info(
                "[AI_INFERENCE_START] requestId=%s inspectionId=%s modelVersionId=%s activeCount=%s",
                request_id,
                inspection_id,
                model_version_id,
                active_count,
            )
            yield
        finally:
            async with self._state_lock:
                self._active_count -= 1
                self._reserved_count -= 1
                active_count = self._active_count
                waiting_count = max(0, self._reserved_count - self._active_count)

            self._semaphore.release()
            logger.info(
                "[AI_INFERENCE_RELEASE] requestId=%s inspectionId=%s modelVersionId=%s activeCount=%s waitingCount=%s",
                request_id,
                inspection_id,
                model_version_id,
                active_count,
                waiting_count,
            )
