from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "industrial-ai-server"
    app_env: str = "local"
    app_port: int = 8001

    chroma_host: str = "localhost"
    chroma_port: int = 8000

    minio_endpoint: str = "localhost:9000"
    minio_access_key: str = "minioadmin"
    minio_secret_key: str = "change_me_minio_password"
    minio_bucket: str = "industrial-ai"
    minio_secure: bool = False

    redis_host: str = "localhost"
    redis_port: int = 6379

    model_name: str = "anomaly-baseline"
    embedding_model_name: str = ""
    llm_model_name: str = ""
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
