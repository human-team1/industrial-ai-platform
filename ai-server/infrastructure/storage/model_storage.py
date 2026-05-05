from __future__ import annotations

from application.exceptions import AppException
from config.settings import Settings
from domain.models.memory_bank import ModelStoragePort
from infrastructure.minio_storage import MinioStorage


class MinioModelStorage(ModelStoragePort):
    def __init__(self, settings: Settings, storage: MinioStorage) -> None:
        self._settings = settings
        self._storage = storage

    def download_model_object(self, object_key: str) -> bytes:
        try:
            return self._storage.download_object(self._settings.minio_bucket_models, object_key)
        except AppException:
            raise
        except Exception as exc:
            raise AppException(404, "Model artifact not found", "모델 산출물을 MinIO에서 찾을 수 없습니다.", "MODEL_ARTIFACT_NOT_FOUND") from exc

    def upload_model_object(self, object_key: str, content: bytes, content_type: str) -> None:
        try:
            self._storage.put_object(self._settings.minio_bucket_models, object_key, content, content_type)
        except Exception as exc:
            raise AppException(500, "Memory bank save failed", "memory_bank 저장에 실패했습니다.", "MEMORY_BANK_SAVE_FAILED") from exc
