from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "industrial-ai-server"
    app_env: str = "local"
    app_port: int = 8001

    chroma_host: str = "localhost"
    chroma_port: int = 8000
    chroma_collection_documents: str = "industrial_document_chunks"

    minio_endpoint: str = "http://localhost:9000"
    minio_access_key: str = "minioadmin"
    minio_secret_key: str = "change_me_minio_password"

    minio_bucket: str = "industrial-ai"  # 추가

    minio_bucket_documents: str = "documents"
    minio_bucket_inspection_artifacts: str = "inspection-artifacts"
    minio_bucket_reports: str = "reports"
    minio_bucket_models: str = "models"
    minio_secure: bool = False

    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: str = ""

    model_name: str = "anomaly-baseline"
    embedding_model_name: str = ""
    llm_model_name: str = ""
    max_inference_concurrency: int = Field(default=1, ge=1)
    inference_queue_size: int = Field(default=5, ge=1)
    log_level: str = "INFO"
    tz: str = "Asia/Seoul"

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        protected_namespaces=("settings_",),
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()
