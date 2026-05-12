from __future__ import annotations

from domain.rag.models import ResultContext


class EmptyResultContextStore:
    def get_by_result_id(self, result_id: str) -> ResultContext | None:
        return None
