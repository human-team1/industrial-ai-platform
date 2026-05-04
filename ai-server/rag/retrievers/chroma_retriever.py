from __future__ import annotations

from typing import Any

from config.settings import Settings
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.embedding_client import EmbeddingClient
from rag.schemas import SourceChunk, SourceChunkMetadata


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
        query: str,
        filters: dict,
        top_k: int = 5,
    ) -> list[SourceChunk]:
        collection = self._chroma_client.get_or_create_document_collection()
        query_embedding = self._embedding_client.embed_query(query)
        internal_top_k = max(top_k, self._settings.retrieval_internal_top_k)

        result = collection.query(
            query_embeddings=[query_embedding],
            n_results=internal_top_k,
            where=self._build_where(filters),
            include=["documents", "metadatas", "distances"],
        )

        return self._to_source_chunks(result, top_k)

    def _build_where(self, filters: dict) -> dict | None:
        conditions = []

        organization_id = filters.get("organization_id")
        if organization_id:
            conditions.append(
                {
                    self._settings.chroma_metadata_org_key: self._normalize_organization_id(
                        organization_id
                    )
                }
            )

        document_status = filters.get("document_status")
        if document_status:
            conditions.append(
                {
                    self._settings.chroma_metadata_document_status_key: str(
                        document_status
                    )
                }
            )

        document_version_id = filters.get("document_version_id")
        if document_version_id:
            conditions.append(
                {
                    self._settings.chroma_metadata_document_version_key: str(
                        document_version_id
                    )
                }
            )

        for key in ["equipment_name", "document_type", "category"]:
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

            source = self._build_source_chunk(
                chunk_id=str(metadata.get("chunk_id") or chunk_id),
                content=content,
                metadata=metadata,
                score=score,
                rank=len(sources) + 1,
            )
            sources.append(source)

            if len(sources) >= top_k:
                break

        return sources

    def _build_source_chunk(
        self,
        chunk_id: str,
        content: str,
        metadata: dict[str, Any],
        score: float | None,
        rank: int,
    ) -> SourceChunk:
        section_title = self._optional_str(metadata.get("section_title"))
        page = self._optional_int(metadata.get("page"))

        if section_title is None and page is None:
            section_title = "위치 미상"

        return SourceChunk(
            chunk_id=chunk_id,
            document_id=str(metadata.get("document_id") or "unknown-document"),
            document_version_id=str(
                metadata.get("document_version_id") or "unknown-document-version"
            ),
            title=str(metadata.get("title") or "제목 없음"),
            document_type=self._optional_str(metadata.get("document_type")),
            category=self._optional_str(metadata.get("category")),
            equipment_name=self._optional_str(metadata.get("equipment_name")),
            section_title=section_title,
            page=page,
            content=content,
            score=score,
            rank=rank,
            source_uri=self._optional_str(metadata.get("source_uri")),
            metadata=SourceChunkMetadata(
                organization_id=str(
                    metadata.get("organization_id")
                    or self._settings.rag_default_organization_id
                ),
                document_status=str(
                    metadata.get("document_status")
                    or self._settings.rag_default_document_status
                ),
                document_version_policy=str(
                    metadata.get("document_version_policy") or "latest_only"
                ),
                chunk_size=self._optional_int(metadata.get("chunk_size"))
                or self._settings.rag_chunk_size,
                chunk_overlap=self._optional_int(metadata.get("chunk_overlap"))
                or self._settings.rag_chunk_overlap,
                embedding_model=str(
                    metadata.get("embedding_model")
                    or self._settings.embedding_model_name
                ),
                vector_ref=self._optional_str(metadata.get("vector_ref")),
                source_group=self._optional_str(metadata.get("source_group")),
                relative_path=self._optional_str(metadata.get("relative_path")),
                created_at=self._optional_str(metadata.get("created_at")),
            ),
        )

    def _distance_to_score(self, distance: Any) -> float | None:
        if distance is None:
            return None

        try:
            # Chroma's default space is L2. With normalized embeddings, squared L2
            # distance is roughly 2 - 2 * cosine_similarity.
            return round(max(0.0, 1.0 - (float(distance) / 2.0)), 6)
        except (TypeError, ValueError):
            return None

    def _normalize_organization_id(self, value: Any) -> str:
        if str(value) == "1":
            return "org-001"
        return str(value)

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
