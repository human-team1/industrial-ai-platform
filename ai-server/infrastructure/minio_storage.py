from config.settings import Settings
from urllib.parse import urlparse


class MinioStorage:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    def put_object(self, object_name: str, content: bytes) -> None:
        # Actual bucket creation and upload policy should be added with storage requirements.
        _ = object_name
        _ = content

    def document_bucket_name(self) -> str:
        return self._settings.minio_bucket_documents

    def is_document_bucket_accessible(self) -> bool:
        try:
            return self.client().bucket_exists(self.document_bucket_name())
        except Exception:
            return False

    def download_object(self, bucket_name: str, object_name: str) -> bytes:
        response = self.client().get_object(bucket_name, object_name)
        try:
            return response.read()
        finally:
            response.close()
            response.release_conn()

    def client(self):
        from minio import Minio

        endpoint = self._normalize_endpoint(self._settings.minio_endpoint)
        return Minio(
            endpoint,
            access_key=self._settings.minio_access_key,
            secret_key=self._settings.minio_secret_key,
            secure=self._settings.minio_secure,
        )

    def _normalize_endpoint(self, endpoint: str) -> str:
        parsed = urlparse(endpoint)
        if parsed.scheme:
            return parsed.netloc
        return endpoint
