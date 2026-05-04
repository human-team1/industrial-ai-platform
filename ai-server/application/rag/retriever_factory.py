from __future__ import annotations

from typing import Any

from config.settings import settings
from infrastructure.chroma_client import ChromaClientWrapper
from infrastructure.embedding_client import EmbeddingClient
from infrastructure.retriever.mock_retriever import MockRetriever


def create_retriever() -> Any:
    """
    settings.retriever_type 기준으로 Retriever 구현체를 생성한다.

    mock:
    - B 라우팅/Guard/no-source/retriever-error 회귀 테스트용

    chroma:
    - A ChromaDB collection 기반 실제 RAG 검색용
    """

    retriever_type = settings.retriever_type.strip().lower()

    if retriever_type == "mock":
        return MockRetriever(settings.mock_sources_path)

    if retriever_type in {"a_chroma", "chroma"}:
        from infrastructure.retriever.chroma_retriever import ChromaRetriever

        chroma_client = ChromaClientWrapper(settings)
        embedding_client = EmbeddingClient(settings)

        return ChromaRetriever(
            settings=settings,
            chroma_client=chroma_client,
            embedding_client=embedding_client,
        )

    raise ValueError(f"unsupported retriever_type: {settings.retriever_type}")