from __future__ import annotations

import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from dotenv import load_dotenv


sys.path.append(str(Path(__file__).resolve().parents[2]))

CHUNK_RECORDS_PATH = Path("experiments/rag_langgraph_eval/results/chunk_records.jsonl")
FALLBACK_CHUNK_RECORDS_PATH = Path(
    "experiments/rag_langgraph_eval/results/chunking_matrix/C3_chunk_records.jsonl"
)
INDEX_BUILD_LOG_PATH = Path("experiments/rag_langgraph_eval/results/retrieval_eval/index_build_log.jsonl")

RETRIEVAL_CONFIG_ID = "R0_GEMINI_BASELINE"
COLLECTION_NAME = "industrial_rag_chunks_a_gemini_baseline"
EMBEDDING_PROVIDER = "gemini"
EMBEDDING_MODEL = "gemini-embedding-001"
CHUNK_SIZE = 800
CHUNK_OVERLAP = 100
SEARCH_MODE = "vector"
TOP_K = 5
DOCUMENT_STATUS_FILTER = "PUBLISHED"


def main() -> None:
    load_dotenv()

    records_path = CHUNK_RECORDS_PATH if CHUNK_RECORDS_PATH.exists() else FALLBACK_CHUNK_RECORDS_PATH
    chunks = load_jsonl(records_path)

    if not chunks:
        raise RuntimeError(f"No chunk records found: {records_path}")

    client = create_chroma_client()
    collection = client.get_or_create_collection(name=COLLECTION_NAME)

    ids = [chunk["chunk_id"] for chunk in chunks]
    documents = [chunk["content"] for chunk in chunks]
    metadatas = [build_metadata(chunk) for chunk in chunks]
    embeddings = embed_texts(documents)

    collection.upsert(
        ids=ids,
        documents=documents,
        metadatas=metadatas,
        embeddings=embeddings,
    )

    log_event(
        {
            "event": "chroma_baseline_index_built",
            "retrieval_config_id": RETRIEVAL_CONFIG_ID,
            "collection_name": COLLECTION_NAME,
            "embedding_provider": EMBEDDING_PROVIDER,
            "embedding_model": EMBEDDING_MODEL,
            "chunk_size": CHUNK_SIZE,
            "chunk_overlap": CHUNK_OVERLAP,
            "search_mode": SEARCH_MODE,
            "top_k": TOP_K,
            "document_status_filter": DOCUMENT_STATUS_FILTER,
            "chunk_count": len(chunks),
            "source_path": str(records_path),
        }
    )

    print(f"collection: {COLLECTION_NAME}")
    print(f"chunks_indexed: {len(chunks)}")
    print(f"log: {INDEX_BUILD_LOG_PATH}")


def load_jsonl(path: Path) -> list[dict[str, Any]]:
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def create_chroma_client():
    import chromadb

    host = os.getenv("CHROMA_HOST", "localhost")
    port = int(os.getenv("CHROMA_PORT", "8003"))
    return chromadb.HttpClient(host=host, port=port)


def embed_texts(texts: list[str]) -> list[list[float]]:
    import requests

    api_key = (
        os.getenv("GEMINI_API_KEY", "").strip()
        or os.getenv("GOOGLE_API_KEY", "").strip()
        or os.getenv("GOOGLE_GENAI_API_KEY", "").strip()
    )
    if not api_key:
        raise RuntimeError("GEMINI_API_KEY or GOOGLE_API_KEY is required to build the Gemini baseline Chroma index.")

    endpoint = f"https://generativelanguage.googleapis.com/v1beta/models/{EMBEDDING_MODEL}:embedContent"
    headers = {
        "x-goog-api-key": api_key,
        "Content-Type": "application/json",
    }
    embeddings: list[list[float]] = []

    for text in texts:
        payload = {
            "model": f"models/{EMBEDDING_MODEL}",
            "content": {"parts": [{"text": text}]},
            "task_type": "RETRIEVAL_DOCUMENT",
            "output_dimensionality": 768,
        }
        response = requests.post(endpoint, headers=headers, json=payload, timeout=60)
        response.raise_for_status()
        embeddings.append(response.json()["embedding"]["values"])

    return embeddings


def build_metadata(chunk: dict[str, Any]) -> dict[str, Any]:
    organization_id = str(chunk.get("organization_id") or "org-001")
    if organization_id == "1":
        organization_id = "org-001"

    return {
        "retrieval_config_id": RETRIEVAL_CONFIG_ID,
        "document_id": chunk.get("document_id", ""),
        "document_version_id": chunk.get("document_version_id", ""),
        "chunk_id": chunk.get("chunk_id", ""),
        "title": chunk.get("title", ""),
        "document_type": chunk.get("document_type", ""),
        "section_title": chunk.get("section_title") or "",
        "page": chunk.get("page") or "",
        "source_group": chunk.get("source_group", ""),
        "relative_path": chunk.get("relative_path", ""),
        "source_uri": chunk.get("source_uri", ""),
        "organization_id": organization_id,
        "document_status": chunk.get("document_status", DOCUMENT_STATUS_FILTER),
        "document_version_policy": "latest_only",
        "chunk_size": CHUNK_SIZE,
        "chunk_overlap": CHUNK_OVERLAP,
        "embedding_provider": EMBEDDING_PROVIDER,
        "embedding_model": EMBEDDING_MODEL,
    }


def log_event(payload: dict[str, Any]) -> None:
    INDEX_BUILD_LOG_PATH.parent.mkdir(parents=True, exist_ok=True)
    row = {
        "created_at": datetime.now(timezone.utc).isoformat(),
        **payload,
    }
    with INDEX_BUILD_LOG_PATH.open("a", encoding="utf-8") as f:
        f.write(json.dumps(row, ensure_ascii=False) + "\n")


if __name__ == "__main__":
    main()
