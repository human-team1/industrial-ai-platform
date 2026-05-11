from __future__ import annotations

from dataclasses import dataclass
from typing import Callable

from application.rag.prompt_builder import PromptBuilder
from application.rag.source_verifier import SourceVerifier
from domain.rag.ports import LLMPort, ResultContextPort, RetrieverPort


@dataclass(frozen=True)
class RagRuntime:
    retriever: RetrieverPort
    result_context_store: ResultContextPort
    llm_client: LLMPort
    prompt_builder_factory: Callable[[str | None], PromptBuilder]
    source_verifier: SourceVerifier
    retriever_type: str
    retrieval_config_id: str | None
    rag_top_k: int
    rag_default_document_status: str
    prompt_version: str
    llm_provider: str
    llm_model_name: str
