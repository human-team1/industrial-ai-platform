from __future__ import annotations

from config.settings import Settings
from container.rag_container import create_retriever as create_rag_retriever


def create_retriever(settings: Settings | None = None):
    return create_rag_retriever(settings)
