from __future__ import annotations

from typing import Any

from config.settings import Settings
from domain.rag.source_chunk import SourceChunk
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.embedding_client import EmbeddingClient


class ChromaRetriever:
    def __init__(
        self,
        settings: Settings,
        chroma_client: ChromaClientWrapper,
        embedding_client: EmbeddingClient,
    ) -> None:
        self._settings = settings
        self._chroma_client = chroma_client
        self._embedding_client = embedding_client

    def search(
        self,
        *,
        query: str,
        filters: dict[str, Any] | None = None,
        top_k: int = 5,
        query_case_id: str | None = None,
    ) -> list[SourceChunk]:
        query = query.strip()
        if not query:
            raise ValueError("query must not be empty")

        filters = filters or {}
        organization_id = filters.get("organization_id")
        if organization_id is not None:
            collection = self._chroma_client.client_instance.get_collection(
                name=self._chroma_client.collection_name_for_organization(int(organization_id))
            )
        else:
            collection = self._chroma_client.get_document_collection()

        effective_top_k = top_k or self._settings.rag_top_k
        internal_top_k = max(effective_top_k, self._settings.retrieval_internal_top_k)
        query_embedding = self._embedding_client.embed_query(query)

        result = collection.query(
            query_embeddings=[query_embedding],
            n_results=internal_top_k,
            where=self._build_where(filters),
            include=["documents", "metadatas", "distances"],
        )
        return self._to_source_chunks(result, effective_top_k)

    def _build_where(self, filters: dict[str, Any]) -> dict | None:
        conditions: list[dict[str, Any]] = []
        organization_id = filters.get("organization_id")
        if organization_id:
            conditions.append({"organizationId": int(organization_id)})

        document_status = filters.get("document_status") or self._settings.rag_default_document_status
        if document_status:
            conditions.append({"document_status": str(document_status)})

        document_version_id = filters.get("document_version_id")
        if document_version_id:
            conditions.append({"documentVersionId": int(document_version_id)})

        for key in ["equipmentType", "documentType", "category"]:
            value = filters.get(key)
            if value:
                conditions.append({key: str(value)})

        if not conditions:
            return None
        if len(conditions) == 1:
            return conditions[0]
        return {"$and": conditions}

    def _to_source_chunks(self, result: dict[str, Any], top_k: int) -> list[SourceChunk]:
        ids = self._first(result.get("ids"))
        documents = self._first(result.get("documents"))
        metadatas = self._first(result.get("metadatas"))
        distances = self._first(result.get("distances"))
        sources: list[SourceChunk] = []

        for index, chunk_id in enumerate(ids):
            metadata = metadatas[index] or {}
            content = documents[index] or metadata.get("content", "")
            score = self._distance_to_score(self._at(distances, index))
            if score is not None and score < self._settings.rag_min_score:
                continue

            sources.append(
                SourceChunk(
                    chunk_id=str(metadata.get("chunkId") or metadata.get("chunk_id") or chunk_id),
                    document_id=str(metadata.get("documentId") or metadata.get("document_id") or "unknown-document"),
                    document_version_id=str(
                        metadata.get("documentVersionId")
                        or metadata.get("document_version_id")
                        or "unknown-document-version"
                    ),
                    title=str(metadata.get("documentTitle") or metadata.get("title") or "제목 없음"),
                    document_type=self._optional_str(metadata.get("documentType") or metadata.get("document_type")),
                    category=self._optional_str(metadata.get("category")),
                    equipment_name=self._optional_str(metadata.get("equipmentType") or metadata.get("equipment_name")),
                    section_title=self._optional_str(metadata.get("section") or metadata.get("section_title")),
                    page=self._optional_int(metadata.get("pageNo") or metadata.get("page")),
                    content=content,
                    score=score,
                    rank=len(sources) + 1,
                    source_uri=self._optional_str(metadata.get("source_uri")),
                    metadata={
                        "organization_id": str(metadata.get("organizationId") or metadata.get("organization_id") or ""),
                        "document_status": str(metadata.get("document_status") or self._settings.rag_default_document_status),
                        "vector_ref": self._optional_str(metadata.get("vector_ref") or metadata.get("chunkId")),
                    },
                )
            )
            if len(sources) >= top_k:
                break
        return sources

    def _distance_to_score(self, distance: Any) -> float | None:
        if distance is None:
            return None
        try:
            return round(max(0.0, 1.0 - (float(distance) / 2.0)), 6)
        except (TypeError, ValueError):
            return None

    def _first(self, values: Any) -> list:
        if not values:
            return []
        return values[0] or []

    def _at(self, values: list, index: int) -> Any:
        if index >= len(values):
            return None
        return values[index]

    def _optional_str(self, value: Any) -> str | None:
        if value is None or value == "":
            return None
        return str(value)

    def _optional_int(self, value: Any) -> int | None:
        if value is None or value == "":
            return None
        try:
            return int(value)
        except (TypeError, ValueError):
            return None
