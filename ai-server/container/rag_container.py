from __future__ import annotations

from config.settings import Settings, get_settings
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.embedding_client import EmbeddingClient
from infrastructure.llm.ollama_llm_client import OllamaLLMClient
from infrastructure.result_context.mock_result_context_store import (
    MockResultContextStore,
)
from infrastructure.retriever.chroma_retriever import ChromaRetriever
from infrastructure.retriever.mock_retriever import MockRetriever
from infrastructure.tracing.langsmith_tracer import LangSmithTracer


def _resolve_settings(settings: Settings | None = None) -> Settings:
    return settings or get_settings()


def create_retriever(settings: Settings | None = None):
    current = _resolve_settings(settings)
    retriever_type = current.retriever_type.lower()

    if retriever_type == "mock":
        return MockRetriever(current.mock_sources_path)

    if retriever_type in {"a_chroma", "chroma"}:
        return ChromaRetriever(
            settings=current,
            chroma_client=ChromaClientWrapper(current),
            embedding_client=EmbeddingClient(current),
        )

    raise ValueError(f"Unsupported retriever_type: {current.retriever_type}")


def create_result_context_store(
    settings: Settings | None = None,
) -> MockResultContextStore:
    current = _resolve_settings(settings)
    return MockResultContextStore(current.result_context_sample_path)


def create_llm_client(settings: Settings | None = None) -> OllamaLLMClient:
    current = _resolve_settings(settings)
    return OllamaLLMClient(
        base_url=current.ollama_base_url,
        model_name=current.llm_model_name,
        temperature=current.llm_temperature,
        max_tokens=current.llm_max_tokens,
        timeout_seconds=current.llm_timeout_seconds,
    )


def create_langsmith_tracer(settings: Settings | None = None) -> LangSmithTracer:
    current = _resolve_settings(settings)
    return LangSmithTracer(current)
