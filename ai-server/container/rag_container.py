from __future__ import annotations

from application.rag.graph_runner import RagGraphRunner, build_rag_graph
from config.settings import Settings, get_settings
from application.rag.prompt_builder import PromptBuilder
from application.rag.runtime import RagRuntime
from application.rag.source_verifier import SourceVerifier
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.embedding_client import EmbeddingClient
from infrastructure.llm.ollama_llm_client import OllamaLLMClient
from infrastructure.result_context.mock_result_context_store import (
    MockResultContextStore,
)
from infrastructure.retriever.chroma_retriever import ChromaRetriever
from infrastructure.tracing.noop_tracer import NoOpTracer


def _resolve_settings(settings: Settings | None = None) -> Settings:
    return settings or get_settings()


def create_retriever(settings: Settings | None = None):
    current = _resolve_settings(settings)
    retriever_type = current.retriever_type.lower()

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


def create_langsmith_tracer(settings: Settings | None = None) -> NoOpTracer:
    _resolve_settings(settings)
    return NoOpTracer()


def create_prompt_builder(
    settings: Settings | None = None,
    *,
    prompt_version: str | None = None,
) -> PromptBuilder:
    current = _resolve_settings(settings)
    return PromptBuilder(
        prompt_dir=current.prompt_dir,
        prompt_version=prompt_version or current.prompt_version,
    )


def create_source_verifier() -> SourceVerifier:
    return SourceVerifier()


def create_rag_runtime(settings: Settings | None = None) -> RagRuntime:
    current = _resolve_settings(settings)
    return RagRuntime(
        retriever=create_retriever(current),
        result_context_store=create_result_context_store(current),
        llm_client=create_llm_client(current),
        prompt_builder_factory=lambda prompt_version: create_prompt_builder(
            current,
            prompt_version=prompt_version,
        ),
        source_verifier=create_source_verifier(),
        retriever_type=current.retriever_type,
        retrieval_config_id=current.retrieval_config_id,
        rag_top_k=current.rag_top_k,
        rag_default_document_status=current.rag_default_document_status,
        rag_default_organization_id=current.rag_default_organization_id,
        prompt_version=current.prompt_version,
        llm_provider=current.llm_provider,
        llm_model_name=current.llm_model_name,
    )


def create_rag_graph(settings: Settings | None = None):
    current = _resolve_settings(settings)
    return build_rag_graph(create_rag_runtime(current))


def create_rag_graph_runner(settings: Settings | None = None):
    current = _resolve_settings(settings)
    return RagGraphRunner(
        graph=create_rag_graph(current),
        tracer=create_langsmith_tracer(current),
    )
