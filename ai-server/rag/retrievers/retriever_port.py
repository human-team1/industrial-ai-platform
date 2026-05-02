from __future__ import annotations

from typing import Protocol

from rag.schemas import SourceChunk


class RetrieverPort(Protocol):
    def search(
        self,
        query: str,
        filters: dict,
        top_k: int = 5,
    ) -> list[SourceChunk]:
        ...
