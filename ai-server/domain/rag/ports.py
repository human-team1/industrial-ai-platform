from __future__ import annotations

from typing import Any, Protocol

from domain.rag.models import LLMGenerateResult, ResultContext
from domain.rag.source_chunk import SourceChunk


class RetrieverPort(Protocol):
    def search(
        self,
        *,
        query: str,
        filters: dict[str, Any] | None = None,
        top_k: int = 5,
        query_case_id: str | None = None,
    ) -> list[SourceChunk]:
        ...


class LLMPort(Protocol):
    def generate(self, *, prompt: str) -> LLMGenerateResult:
        ...


class ResultContextPort(Protocol):
    def get_by_result_id(self, result_id: str) -> ResultContext | None:
        ...


class TracePort(Protocol):
    def configure_environment(self) -> None:
        ...

    def build_metadata(
        self,
        *,
        state: Any | None = None,
        query_case_id: str | None = None,
        extra: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        ...

    def build_tags(
        self,
        *,
        state: Any | None = None,
        extra_tags: list[str] | None = None,
    ) -> list[str]:
        ...

    def runnable_config(
        self,
        *,
        state: Any | None = None,
        query_case_id: str | None = None,
        extra: dict[str, Any] | None = None,
        extra_tags: list[str] | None = None,
    ) -> dict[str, Any]:
        ...


class RagCollectionPort(Protocol):
    def collection_name(self) -> str: ...
