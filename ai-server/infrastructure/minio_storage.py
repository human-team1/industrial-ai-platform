from io import BytesIO
from config.settings import Settings
from urllib.parse import urlparse
import urllib3

from application.exceptions import AppException


class MinioStorage:
    CONNECT_TIMEOUT_SECONDS = 3.0
    READ_TIMEOUT_SECONDS = 10.0

    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._client = None

    def put_object(self, bucket_name: str, object_name: str, content: bytes, content_type: str = "application/octet-stream") -> None:
        payload = BytesIO(content)
        client = self.client()
        if not client.bucket_exists(bucket_name):
            client.make_bucket(bucket_name)
        client.put_object(
            bucket_name,
            object_name,
            payload,
            length=len(content),
            content_type=content_type,
        )

    def document_bucket_name(self) -> str:
        return self._settings.minio_bucket_documents

    def is_document_bucket_accessible(self) -> bool:
        try:
            return self.client().bucket_exists(self.document_bucket_name())
        except Exception:
            return False

    def health_check(self) -> bool:
        return self.is_document_bucket_accessible()

    def download_object(self, bucket_name: str, object_name: str) -> bytes:
        try:
            response = self.client().get_object(bucket_name, object_name)
        except Exception as exc:
            raise AppException(404, "Object not found", "저장소에서 파일을 찾을 수 없습니다.", "AI_OBJECT_NOT_FOUND") from exc
        try:
            return response.read()
        finally:
            response.close()
            response.release_conn()

    def client(self):
        from minio import Minio

        if self._client is not None:
            return self._client
        endpoint = self._normalize_endpoint(self._settings.minio_endpoint)
        self._client = Minio(
            endpoint,
            access_key=self._settings.minio_access_key,
            secret_key=self._settings.minio_secret_key,
            secure=self._settings.minio_secure,
            http_client=urllib3.PoolManager(
                timeout=urllib3.Timeout(connect=self.CONNECT_TIMEOUT_SECONDS, read=self.READ_TIMEOUT_SECONDS),
                retries=False,
            ),
        )
        return self._client

    def _normalize_endpoint(self, endpoint: str) -> str:
        parsed = urlparse(endpoint)
        if parsed.scheme:
            return parsed.netloc
        return endpoint
