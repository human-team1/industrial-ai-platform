from __future__ import annotations

import os
from functools import cached_property
from pathlib import Path

import requests

from config.settings import Settings


class EmbeddingClient:
    """
    Query embedding 생성 클라이언트.

    A 최종 Chroma collection은 BAAI/bge-m3 기준이므로
    B 검색 query도 동일한 모델로 embedding해야 한다.
    """

    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    def embed_query(self, text: str) -> list[float]:
        return self.embed_texts([text])[0]

    def embed_texts(self, texts: list[str]) -> list[list[float]]:
        provider = self._settings.embedding_provider.lower()

        if provider in {"local", "sentence_transformers", "sentence-transformers"}:
            return self._embed_with_sentence_transformers(texts)

        if provider == "gemini":
            return self._embed_with_gemini(texts)

        if provider == "openai":
            return self._embed_with_openai(texts)

        raise ValueError(
            f"Unsupported embedding provider: {self._settings.embedding_provider}"
        )

    @cached_property
    def _sentence_transformer(self):
        cache_dir = Path(self._settings.embedding_cache_dir).resolve()
        hub_cache_dir = cache_dir / "hub"
        hub_cache_dir.mkdir(parents=True, exist_ok=True)

        os.environ["HF_HOME"] = str(cache_dir)
        os.environ["HUGGINGFACE_HUB_CACHE"] = str(hub_cache_dir)
        os.environ["HF_HUB_DISABLE_SYMLINKS_WARNING"] = "1"

        from sentence_transformers import SentenceTransformer

        return SentenceTransformer(
            self._settings.embedding_model_name,
            cache_folder=str(cache_dir),
            local_files_only=self._settings.embedding_local_files_only,
        )

    def _embed_with_sentence_transformers(self, texts: list[str]) -> list[list[float]]:
        vector = self._sentence_transformer.encode(
            texts,
            normalize_embeddings=True,
            show_progress_bar=False,
        )
        return vector.tolist()

    def _embed_with_gemini(self, texts: list[str]) -> list[list[float]]:
        api_key = (
            self._settings.gemini_api_key
            or os.getenv("GEMINI_API_KEY")
            or os.getenv("GOOGLE_API_KEY")
            or os.getenv("GOOGLE_GENAI_API_KEY")
        )
        if not api_key:
            raise RuntimeError("GEMINI_API_KEY is required for Gemini embedding")

        endpoint = (
            "https://generativelanguage.googleapis.com/v1beta/"
            f"models/{self._settings.embedding_model_name}:embedContent"
        )

        vectors: list[list[float]] = []

        for text in texts:
            response = requests.post(
                endpoint,
                headers={
                    "x-goog-api-key": api_key,
                    "Content-Type": "application/json",
                },
                json={
                    "model": f"models/{self._settings.embedding_model_name}",
                    "content": {"parts": [{"text": text}]},
                    "task_type": "RETRIEVAL_QUERY",
                    "output_dimensionality": 768,
                },
                timeout=30,
            )
            response.raise_for_status()
            vectors.append(response.json()["embedding"]["values"])

        return vectors

    # def _embed_with_openai(self, texts: list[str]) -> list[list[float]]:
    #     from openai import OpenAI

    #     api_key = os.getenv("OPENAI_API_KEY")
    #     if not api_key:
    #         raise RuntimeError("OPENAI_API_KEY is required for OpenAI embedding")

    #     client = OpenAI(api_key=api_key)
    #     response = client.embeddings.create(
    #         model=self._settings.embedding_model_name,
    #         input=texts,
    #     )
    #     return [item.embedding for item in response.data]