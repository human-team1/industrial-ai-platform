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
        return self._download_object(object_key, "MODEL_NOT_FOUND", "Model object not found")

    def download_config_object(self, object_key: str) -> bytes:
        return self._download_object(object_key, "MODEL_CONFIG_NOT_FOUND", "Model config not found")

    def download_ckpt_object(self, object_key: str) -> bytes:
        return self._download_object(object_key, "MODEL_CKPT_NOT_FOUND", "Model checkpoint not found")

    def download_normal_image_object(self, object_key: str) -> bytes:
        return self._download_object(object_key, "NORMAL_IMAGE_NOT_FOUND", "Normal image not found")

    def upload_model_object(self, object_key: str, content: bytes, content_type: str) -> None:
        try:
            self._storage.put_object(self._settings.minio_bucket_models, object_key, content, content_type)
        except Exception as exc:
            raise AppException(500, "Memory bank upload failed", "memory_bank upload failed.", "MEMORY_BANK_UPLOAD_FAILED") from exc

    def _download_object(self, object_key: str, code: str, title: str) -> bytes:
        try:
            return self._storage.download_object(self._settings.minio_bucket_models, object_key)
        except AppException as exc:
            if exc.status_code == 404:
                raise AppException(404, title, f"MinIO object not found: {object_key}", code) from exc
            raise AppException(exc.status_code, title, f"MinIO object download failed: {object_key}", code) from exc
        except TimeoutError as exc:
            raise AppException(500, title, f"MinIO object download timed out: {object_key}", code) from exc
        except OSError as exc:
            raise AppException(500, title, f"MinIO object download failed: {object_key}", code) from exc
        except Exception as exc:
            raise AppException(500, title, f"MinIO object download failed: {object_key}", code) from exc
