from config.settings import Settings


class RedisStatusStore:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    def set_status(self, key: str, status: str) -> None:
        # Persisting runtime job status belongs here once async workers are introduced.
        _ = key
        _ = status

    def client(self):
        import redis

        return redis.Redis(
            host=self._settings.redis_host,
            port=self._settings.redis_port,
            decode_responses=True,
        )
