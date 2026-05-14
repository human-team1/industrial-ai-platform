from __future__ import annotations

from typing import Any

from domain.rag.query_models import RagQueryResult, RagSourceResult
from domain.rag.state import GraphState


def build_rag_query_response(state: GraphState) -> RagQueryResult:
    """
    GraphState를 API 응답용 data payload로 변환한다.
    """

    return RagQueryResult(
        answer=state.answer or "",
        answer_type=state.answer_type,
        question_mode=state.question_mode,
        citation_ok=state.citation_ok,
        sources=[to_source_response(source) for source in state.sources],
        need_clarification=state.need_clarification,
        safety_flags=list(state.safety_flags),
        source_warnings=list(state.source_warnings),
        errors=list(state.errors),
        result_id=state.result_id,
        metadata={
            "answerStatus": normalize_answer_status(state),
            "retriever_type": state.retriever_type,
            "retrieval_config_id": state.retrieval_config_id,
            "prompt_version": state.prompt_version,
            "llm_provider": state.llm_provider,
            "llmModel": state.llm_model or state.llm_model_name,
            "llm_model": state.llm_model or state.llm_model_name,
            "llm_latency_ms": state.llm_latency_ms,
            "source_count": len(state.sources),
            "citation_ok": state.citation_ok,
            "route_path": list(state.route_path),
        },
    )


def to_source_response(source: Any) -> RagSourceResult:
    data = _to_dict(source)

    return RagSourceResult(
        document_id=str(data.get("document_id") or data.get("doc_id")),
        document_version_id=data.get("document_version_id"),
        chunk_id=str(data.get("chunk_id")),
        title=str(data.get("title") or "제목 없음"),
        document_type=data.get("document_type"),
        section_title=data.get("section_title") or data.get("section"),
        page=data.get("page"),
        score=data.get("score"),
        source_snippet=make_snippet(data.get("content")),
        source_uri=normalize_source_uri(data.get("source_uri")),
    )


def _to_dict(source: Any) -> dict[str, Any]:
    if isinstance(source, dict):
        return source

    if hasattr(source, "model_dump"):
        return source.model_dump()

    if hasattr(source, "dict"):
        return source.dict()

    raise TypeError(f"unsupported source type: {type(source)}")


def make_snippet(content: str | None, limit: int = 220) -> str | None:
    if not content:
        return None

    text = " ".join(str(content).split())

    if len(text) <= limit:
        return text

    return text[:limit].rstrip() + "..."


def normalize_source_uri(source_uri: str | None) -> str | None:
    if not source_uri:
        return None

    # 로컬 corpus 경로는 외부 API 응답에 노출하지 않는다.
    if "rag\\corpus\\" in source_uri or "rag/corpus/" in source_uri:
        return None

    return source_uri


def normalize_answer_status(state: GraphState) -> str:
    answer_type_raw = state.answer_type.value if hasattr(state.answer_type, "value") else state.answer_type
    answer_type = str(answer_type_raw or "").strip().lower()
    if answer_type in {"result_linked", "document_search", "general"}:
        return "ANSWERED"
    if answer_type == "no_retrieval_result":
        return "NO_RELEVANT_SOURCE"
    if answer_type == "out_of_scope":
        return "OUT_OF_SCOPE"
    if answer_type == "retriever_error":
        return "VECTOR_STORE_FAILED"
    if answer_type == "validation_error":
        return "VALIDATION_FAILED"
    if answer_type == "need_result_context":
        return "VALIDATION_FAILED"
    if answer_type == "system_error":
        return "LLM_FAILED"
    return "LLM_FAILED"
