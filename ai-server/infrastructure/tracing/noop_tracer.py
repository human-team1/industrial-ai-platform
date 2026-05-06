from __future__ import annotations

from typing import Any


class NoOpTracer:
    """
    LangSmith 없이 LangGraph 실행만 가능하게 하는 no-op tracer.
    """

    def configure_environment(self) -> None:
        return None

    def build_metadata(
        self,
        *,
        state: Any | None = None,
        query_case_id: str | None = None,
        extra: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        return dict(extra or {})

    def build_tags(
        self,
        *,
        state: Any | None = None,
        extra_tags: list[str] | None = None,
    ) -> list[str]:
        return list(extra_tags or [])

    def runnable_config(
        self,
        *,
        state: Any | None = None,
        query_case_id: str | None = None,
        extra: dict[str, Any] | None = None,
        extra_tags: list[str] | None = None,
    ) -> dict[str, Any]:
        metadata = self.build_metadata(
            state=state,
            query_case_id=query_case_id,
            extra=extra,
        )
        tags = self.build_tags(
            state=state,
            extra_tags=extra_tags,
        )

        config: dict[str, Any] = {}
        if metadata:
            config["metadata"] = metadata
        if tags:
            config["tags"] = tags
        return config
