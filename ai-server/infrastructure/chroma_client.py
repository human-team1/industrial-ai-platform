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

    def collection_name_for_organization(self, organization_id: int) -> str:
        return f"documents_org_{organization_id}"

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
        collection_name: str | None = None,
    ) -> int:
        resolved_collection_name = collection_name
        if resolved_collection_name is None:
            resolved_collection_name = (
                self.collection_name_for_organization(request.organization_id)
                if request.organization_id is not None
                else self._settings.chroma_collection_documents
            )
        collection = self.client_instance.get_or_create_collection(
            name=resolved_collection_name
        )
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
                "organizationId": request.organization_id,
                "documentId": request.document_id,
                "documentVersionId": request.document_version_id,
                "fileId": request.file_id,
                "fileKey": request.file_key,
                "documentType": str(request.document_type).upper(),
                "documentTitle": request.document_title,
                "category": request.category,
                "equipmentType": request.equipment_type,
                "sequenceNo": chunk.sequence_no,
                "chunkId": chunk.vector_ref,
                "document_status": self._settings.rag_default_document_status,
                "organization_id": str(request.organization_id),
                "document_id": str(request.document_id),
                "document_version_id": str(request.document_version_id),
                "title": request.document_title,
                "document_type": str(request.document_type).upper(),
                "equipment_type": request.equipment_type,
            }
            if chunk.page_no is not None:
                metadata["pageNo"] = chunk.page_no
            if chunk.section:
                metadata["section"] = chunk.section
            metadatas.append({key: value for key, value in metadata.items() if value is not None})

        collection.add(
            ids=existing_ids,
            documents=[chunk.content for chunk in chunks],
            embeddings=embeddings,
            metadatas=metadatas,
        )
        return len(chunks)

    def delete_document_version(self, collection_name: str, document_version_id: int) -> int:
        try:
            collection = self.client_instance.get_collection(name=collection_name)
        except Exception as exc:
            if "connect" in str(exc).lower():
                raise
            return 0

        ids: list[str] = []
        try:
            result = collection.get(where={"documentVersionId": document_version_id})
            ids = [str(value) for value in result.get("ids", [])]
        except Exception:
            try:
                result = collection.get(where={"document_version_id": str(document_version_id)})
                ids = [str(value) for value in result.get("ids", [])]
            except Exception:
                ids = []

        if not ids:
            return 0
        collection.delete(ids=ids)
        return len(ids)

    def get_dev_artifacts_collection(self):
        return self.client_instance.get_collection(
            name=self._settings.chroma_dev_artifacts_collection
        )
