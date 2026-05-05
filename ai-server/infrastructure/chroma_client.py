from __future__ import annotations

import chromadb

from config.settings import Settings


class ChromaClientWrapper:
    """
    ChromaDB 연결 래퍼.

    최종 기준:
    - ChromaDB: localhost:8000
    - main collection: industrial_rag_chunks_a_v1
    - dev artifacts collection: industrial_rag_dev_artifacts_v1
    """

    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._client = chromadb.HttpClient(
            host=settings.chroma_host,
            port=settings.chroma_port,
        )

    def get_document_collection(self):
        """
        검색용 collection 조회.

        get_or_create를 쓰지 않는다.
        collection 이름이 틀렸거나 Chroma가 비어 있으면 바로 실패해야
        smoke test에서 문제를 발견할 수 있다.
        """
        return self._client.get_collection(
            name=self._settings.chroma_collection_documents
        )
        
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
        """
        인덱싱/개발용.
        검색 retriever에서는 get_document_collection() 사용 권장.
        """
        return self._client.get_or_create_collection(
            name=self._settings.chroma_collection_documents
        )

    def get_dev_artifacts_collection(self):
        return self._client.get_collection(
            name=self._settings.chroma_dev_artifacts_collection
        )