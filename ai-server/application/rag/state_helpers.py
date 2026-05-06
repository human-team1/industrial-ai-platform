from __future__ import annotations

from typing import Any
from domain.rag.source_chunk import SourceChunk
from domain.rag.state import GraphState


def to_state(state: GraphState | dict[str, Any]) -> GraphState:
    if isinstance(state, GraphState):
        return state
    return GraphState.model_validate(state)


def append_route(state: GraphState, node_name: str) -> list[str]:
    return [*state.route_path, node_name]


def normalize_organization_id_for_retrieval(
    value: Any,
    default_organization_id: str,
) -> str:
    if value is None:
        normalized_default = str(default_organization_id).strip()
        if normalized_default.lower().startswith("org-"):
            suffix = normalized_default[4:]
            if suffix.isdigit():
                return suffix
        return normalized_default

    return str(value).strip()


def source_to_source_chunk(source: Any) -> SourceChunk:
    if isinstance(source, SourceChunk):
        return source

    if isinstance(source, dict):
        return SourceChunk.model_validate(source)

    if hasattr(source, "model_dump"):
        return SourceChunk.model_validate(source.model_dump())

    if hasattr(source, "dict"):
        return SourceChunk.model_validate(source.dict())

    raise TypeError(f"unsupported source type: {type(source)}")


def normalize_retrieval_sources(result: Any) -> list[SourceChunk]:
    if isinstance(result, list):
        return [source_to_source_chunk(source) for source in result]

    if hasattr(result, "sources"):
        return [source_to_source_chunk(source) for source in result.sources]

    if isinstance(result, dict) and "sources" in result:
        return [source_to_source_chunk(source) for source in result["sources"]]

    raise TypeError(f"unsupported retrieval result type: {type(result)}")


def get_context_value(result_context: Any | None, key: str) -> Any:
    if result_context is None:
        return None

    if isinstance(result_context, dict):
        value = result_context.get(key)
    else:
        value = getattr(result_context, key, None)

    if hasattr(value, "value"):
        return value.value

    return value


def compact_unique_text(parts: list[Any]) -> str:
    seen: set[str] = set()
    values: list[str] = []

    for part in parts:
        if part is None:
            continue

        text = str(part).strip()
        if not text or text == "None":
            continue

        if text not in seen:
            seen.add(text)
            values.append(text)

    return " ".join(values)
