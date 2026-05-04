from config.settings import Settings


class ChromaClientWrapper:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    def base_url(self) -> str:
        return f"http://{self._settings.chroma_host}:{self._settings.chroma_port}"

    def collection_name(self) -> str:
        return self._settings.chroma_collection_name

    def client(self):
        import chromadb
        from chromadb.config import Settings as ChromaSettings

        return chromadb.HttpClient(
            host=self._settings.chroma_host,
            port=self._settings.chroma_port,
            settings=ChromaSettings(anonymized_telemetry=False),
        )

    def get_or_create_document_collection(self):
        return self.client().get_or_create_collection(name=self.collection_name())

    def health_check(self) -> bool:
        try:
            import requests

            response = requests.get(f"{self.base_url()}/api/v1/heartbeat", timeout=2)
            response.raise_for_status()
            return True
        except Exception:
            return False
