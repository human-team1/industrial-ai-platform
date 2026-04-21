from config.settings import Settings


class MinioStorage:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    def put_object(self, object_name: str, content: bytes) -> None:
        # Actual bucket creation and upload policy should be added with storage requirements.
        _ = object_name
        _ = content

    def client(self):
        from minio import Minio

        return Minio(
            self._settings.minio_endpoint,
            access_key=self._settings.minio_access_key,
            secret_key=self._settings.minio_secret_key,
            secure=self._settings.minio_secure,
        )
