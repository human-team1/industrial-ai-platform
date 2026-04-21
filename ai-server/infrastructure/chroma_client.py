from config.settings import Settings


class ChromaClientWrapper:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    def base_url(self) -> str:
        return f"http://{self._settings.chroma_host}:{self._settings.chroma_port}"

    def collection_name(self) -> str:
        return "industrial_knowledge"

    def client(self):
        import chromadb

        return chromadb.HttpClient(
            host=self._settings.chroma_host,
            port=self._settings.chroma_port,
        )
