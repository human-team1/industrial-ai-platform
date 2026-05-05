from __future__ import annotations

import chromadb

from config.settings import Settings
from domain.document_models import DocumentIndexCommand, IndexedChunk


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
        self._client = None

    @property
    def client_instance(self):
        if self._client is None:
            self._client = chromadb.HttpClient(
                host=self._settings.chroma_host,
                port=self._settings.chroma_port,
            )
        return self._client

    def get_document_collection(self):
        """
        검색용 collection 조회.

        get_or_create를 쓰지 않는다.
        collection 이름이 틀렸거나 Chroma가 비어 있으면 바로 실패해야
        smoke test에서 문제를 발견할 수 있다.
        """
        return self.client_instance.get_collection(
            name=self._settings.chroma_collection_documents
        )
        
    def collection_name(self) -> str:
        return self._settings.chroma_collection_name

    def health_check(self) -> bool:
        try:
            self.client_instance.heartbeat()
            return True
        except Exception:
            return False

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
        return self.client_instance.get_or_create_collection(
            name=self._settings.chroma_collection_documents
        )

    def upsert_document_chunks(
        self,
        request: DocumentIndexCommand,
        chunks: list[IndexedChunk],
        embeddings: list[list[float]],
    ) -> int:
        collection = self.get_or_create_document_collection()
        existing_ids = [chunk.vector_ref for chunk in chunks]
        try:
            collection.delete(where={"documentVersionId": request.document_version_id})
        except Exception:
            if existing_ids:
                try:
                    collection.delete(ids=existing_ids)
                except Exception:
                    pass

        metadatas = []
        for chunk in chunks:
            metadata = {
                "documentId": request.document_id,
                "documentVersionId": request.document_version_id,
                "fileId": request.file_id,
                "fileKey": request.file_key,
                "documentType": str(request.document_type).upper(),
                "sequenceNo": chunk.sequence_no,
            }
            if chunk.page_no is not None:
                metadata["pageNo"] = chunk.page_no
            if chunk.section:
                metadata["section"] = chunk.section
            metadatas.append(metadata)

        collection.add(
            ids=existing_ids,
            documents=[chunk.content for chunk in chunks],
            embeddings=embeddings,
            metadatas=metadatas,
        )
        return len(chunks)

    def get_dev_artifacts_collection(self):
        return self.client_instance.get_collection(
            name=self._settings.chroma_dev_artifacts_collection
        )
