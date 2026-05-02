from __future__ import annotations

import os
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from dotenv import load_dotenv


sys.path.append(str(Path(__file__).resolve().parents[2]))

COLLECTION_NAME = "industrial_rag_chunks_a_gemini_baseline"
EMBEDDING_PROVIDER = "gemini"
EMBEDDING_MODEL = "gemini-embedding-001"
TOP_K = 5
MIN_SCORE = 0.35


@dataclass(frozen=True)
class ValidationCase:
    case_id: str
    query: str
    expected_keyword: str
    should_hit: bool


CASES = [
    ValidationCase(
        case_id="Q1",
        query="FlexLink X65 컨베이어 정렬 문제 조치 방법",
        expected_keyword="FlexLink",
        should_hit=True,
    ),
    ValidationCase(
        case_id="Q2",
        query="조명 반사 때문에 heatmap이 넓게 나올 때 확인할 항목",
        expected_keyword="Heatmap",
        should_hit=True,
    ),
    ValidationCase(
        case_id="Q3",
        query="커피머신 물탱크 청소 방법",
        expected_keyword="커피머신",
        should_hit=False,
    ),
]


def main() -> None:
    load_dotenv()
    collection = create_chroma_client().get_collection(name=COLLECTION_NAME)

    failures = []
    for case in CASES:
        started = time.perf_counter()
        result = search(collection, case.query)
        latency_ms = round((time.perf_counter() - started) * 1000, 2)
        sources = normalize_result(result)
        hit = has_expected_hit(sources, case.expected_keyword, case.should_hit)
        status = "PASS" if hit else "FAIL"

        print(f"[{status}] {case.case_id} latency_ms={latency_ms} sources={len(sources)}")
        for source in sources[:3]:
            print(
                f"  rank={source['rank']} score={source['score']:.4f} "
                f"title={source['title']} section={source['section_title']}"
            )

        if not hit:
            failures.append(case.case_id)

    if failures:
        raise RuntimeError(f"Index validation failed: {', '.join(failures)}")

    print("Index validation passed.")


def create_chroma_client():
    import chromadb

    host = os.getenv("CHROMA_HOST", "localhost")
    port = int(os.getenv("CHROMA_PORT", "8003"))
    return chromadb.HttpClient(host=host, port=port)


def search(collection, query: str) -> dict[str, Any]:
    embedding = embed_query(query)
    return collection.query(
        query_embeddings=[embedding],
        n_results=TOP_K,
        where={
            "$and": [
                {"organization_id": {"$eq": os.getenv("RAG_DEFAULT_ORGANIZATION_ID", "org-001")}},
                {"document_status": {"$eq": "PUBLISHED"}},
            ]
        },
        include=["documents", "metadatas", "distances"],
    )


def embed_query(query: str) -> list[float]:
    import requests

    api_key = (
        os.getenv("GEMINI_API_KEY", "").strip()
        or os.getenv("GOOGLE_API_KEY", "").strip()
        or os.getenv("GOOGLE_GENAI_API_KEY", "").strip()
    )
    if not api_key:
        raise RuntimeError("GEMINI_API_KEY or GOOGLE_API_KEY is required to validate the Gemini baseline Chroma index.")

    endpoint = f"https://generativelanguage.googleapis.com/v1beta/models/{EMBEDDING_MODEL}:embedContent"
    payload = {
        "model": f"models/{EMBEDDING_MODEL}",
        "content": {"parts": [{"text": query}]},
        "task_type": "RETRIEVAL_QUERY",
        "output_dimensionality": 768,
    }
    response = requests.post(
        endpoint,
        headers={
            "x-goog-api-key": api_key,
            "Content-Type": "application/json",
        },
        json=payload,
        timeout=60,
    )
    response.raise_for_status()
    return response.json()["embedding"]["values"]


def normalize_result(result: dict[str, Any]) -> list[dict[str, Any]]:
    metadatas = result.get("metadatas", [[]])[0]
    documents = result.get("documents", [[]])[0]
    distances = result.get("distances", [[]])[0]
    sources = []

    for idx, metadata in enumerate(metadatas, start=1):
        distance = float(distances[idx - 1]) if idx - 1 < len(distances) else 1.0
        score = max(0.0, 1.0 - distance)
        if score < MIN_SCORE:
            continue

        sources.append(
            {
                "rank": idx,
                "score": score,
                "title": metadata.get("title", ""),
                "section_title": metadata.get("section_title", ""),
                "content": documents[idx - 1] if idx - 1 < len(documents) else "",
            }
        )

    return sources


def has_expected_hit(sources: list[dict[str, Any]], expected_keyword: str, should_hit: bool) -> bool:
    searchable = "\n".join(
        f"{source['title']}\n{source['section_title']}\n{source['content']}" for source in sources
    ).lower()
    found = expected_keyword.lower() in searchable
    return found if should_hit else not found


if __name__ == "__main__":
    main()
