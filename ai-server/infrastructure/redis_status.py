from config.settings import Settings
from datetime import timedelta


class RedisStatusStore:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    def set_status(self, key: str, status: str) -> None:
        self.client().set(key, status, ex=timedelta(days=1))

    def get_status(self, key: str) -> str | None:
        return self.client().get(key)

    def set_job_status(self, job_type: str, job_id: str, status: str, ttl_seconds: int = 86400) -> None:
        self.client().set(self.job_key(job_type, job_id), status, ex=ttl_seconds)

    def get_job_status(self, job_type: str, job_id: str) -> str | None:
        return self.client().get(self.job_key(job_type, job_id))

    def job_key(self, job_type: str, job_id: str) -> str:
        return f"job:{job_type}:{job_id}"

    def ping(self) -> bool:
        try:
            return bool(self.client().ping())
        except Exception:
            return False

    def health_check(self) -> bool:
        return self.ping()

    def client(self):
        import redis

        return redis.Redis(
            host=self._settings.redis_host,
            port=self._settings.redis_port,
            password=self._settings.redis_password or None,
            decode_responses=True,
        )
