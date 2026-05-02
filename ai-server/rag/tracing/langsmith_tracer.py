from __future__ import annotations

import os
import time
from dataclasses import dataclass, field
from typing import Any


@dataclass
class RetrievalTracePayload:
    query: str
    sources: list[dict[str, Any]]
    metadata: dict[str, Any]
    filters: dict[str, Any] = field(default_factory=dict)
    latency_ms: float | None = None


class RetrievalTracer:
    """Optional LangSmith tracer for A-side retrieval experiments."""

    def __init__(
        self,
        project_name: str | None = None,
        enabled: bool | None = None,
    ) -> None:
        self.project_name = project_name or os.getenv("LANGSMITH_PROJECT", "industrial-rag-eval")
        self.enabled = self._resolve_enabled(enabled)

    def trace(self, payload: RetrievalTracePayload) -> dict[str, Any]:
        if not self.enabled:
            return {"traced": False, "reason": "disabled"}

        try:
            from langsmith import Client

            client = Client()
            run = client.create_run(
                name="retrieval_eval",
                run_type="retriever",
                project_name=self.project_name,
                inputs={
                    "query": payload.query,
                    "filters": payload.filters,
                },
                outputs={
                    "sources": payload.sources,
                    "latency_ms": payload.latency_ms,
                },
                extra={"metadata": payload.metadata},
            )
            return {"traced": True, "run_id": str(getattr(run, "id", ""))}
        except Exception as exc:
            return {"traced": False, "reason": exc.__class__.__name__}

    def trace_search(
        self,
        query: str,
        filters: dict[str, Any],
        retrieval_config_id: str,
        search_fn,
        metadata: dict[str, Any] | None = None,
    ):
        started = time.perf_counter()
        sources = search_fn()
        latency_ms = round((time.perf_counter() - started) * 1000, 2)
        source_dicts = [
            source.model_dump() if hasattr(source, "model_dump") else dict(source)
            for source in sources
        ]
        merged_metadata = {
            "team": "A",
            "stage": "retrieval_eval",
            "retrieval_config_id": retrieval_config_id,
            **(metadata or {}),
        }
        self.trace(
            RetrievalTracePayload(
                query=query,
                filters=filters,
                sources=source_dicts,
                metadata=merged_metadata,
                latency_ms=latency_ms,
            )
        )
        return sources

    @staticmethod
    def build_metadata(
        query_case_id: str | None,
        retrieval_config_id: str,
        embedding_model: str,
        chunk_size: int,
        chunk_overlap: int,
        search_mode: str,
        top_k: int,
        expected_doc_hit: bool | None = None,
    ) -> dict[str, Any]:
        return {
            "team": "A",
            "stage": "retrieval_eval",
            "query_case_id": query_case_id,
            "retrieval_config_id": retrieval_config_id,
            "embedding_model": embedding_model,
            "chunk_size": chunk_size,
            "chunk_overlap": chunk_overlap,
            "search_mode": search_mode,
            "top_k": top_k,
            "expected_doc_hit": expected_doc_hit,
        }

    @staticmethod
    def _resolve_enabled(enabled: bool | None) -> bool:
        if enabled is not None:
            return enabled

        tracing = os.getenv("LANGSMITH_TRACING", "false").lower() == "true"
        api_key = bool(os.getenv("LANGSMITH_API_KEY"))
        return tracing and api_key
