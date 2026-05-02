from __future__ import annotations

import json
from pathlib import Path

from rag.schemas import SourceChunk


class MockRetriever:
    def __init__(
        self,
        sources_path: str | Path = "experiments/rag_langgraph_eval/datasets/mock_sources_v1.json",
    ) -> None:
        self.sources_path = Path(sources_path)
        self.sources = self._load_sources()

    def search(
        self,
        query: str,
        filters: dict,
        top_k: int = 5,
    ) -> list[SourceChunk]:
        query_terms = normalize_terms(query)
        filtered_sources = [
            source
            for source in self.sources
            if matches_filters(source, filters)
        ]
        ranked_sources = sorted(
            filtered_sources,
            key=lambda source: (
                text_match_score(source, query_terms),
                source.score or 0.0,
            ),
            reverse=True,
        )

        return [
            source.model_copy(update={"rank": rank})
            for rank, source in enumerate(ranked_sources[:top_k], start=1)
        ]

    def _load_sources(self) -> list[SourceChunk]:
        if not self.sources_path.exists():
            raise FileNotFoundError(f"Mock source file not found: {self.sources_path}")

        raw_sources = json.loads(self.sources_path.read_text(encoding="utf-8"))
        return [SourceChunk(**source) for source in raw_sources]


def matches_filters(source: SourceChunk, filters: dict) -> bool:
    if not filters:
        return True

    for key in ["equipment_name", "document_type", "category"]:
        expected = filters.get(key)
        if expected and str(getattr(source, key, "")) != str(expected):
            return False

    document_status = filters.get("document_status")
    if document_status and source.metadata:
        return source.metadata.document_status == document_status

    return True


def normalize_terms(text: str) -> set[str]:
    return {
        token.lower()
        for token in text.replace("/", " ").replace("_", " ").split()
        if len(token.strip()) >= 2
    }


def text_match_score(source: SourceChunk, query_terms: set[str]) -> int:
    if not query_terms:
        return 0

    searchable = " ".join(
        [
            source.title,
            source.section_title or "",
            source.content,
            source.equipment_name or "",
            source.category or "",
        ]
    ).lower()
    return sum(1 for term in query_terms if term in searchable)
