from __future__ import annotations

import hashlib
import json
import sys
from pathlib import Path
from typing import Any

from dotenv import load_dotenv


sys.path.append(str(Path(__file__).resolve().parents[2]))

from config.settings import Settings  # noqa: E402
from infrastructure.chroma_client import ChromaClientWrapper  # noqa: E402
from infrastructure.embedding_client import EmbeddingClient  # noqa: E402


COLLECTION_NAME = "industrial_rag_dev_artifacts_v1"
MAX_CHARS = 1800
OVERLAP = 200

ARTIFACT_PATHS = [
    "rag/schemas/source_chunk.py",
    "rag/schemas/retrieval_result.py",
    "rag/schemas/retrieval_config.py",
    "experiments/rag_langgraph_eval/datasets/mock_sources_v1.json",
    "experiments/rag_langgraph_eval/datasets/golden_questions_v1.csv",
    "experiments/rag_langgraph_eval/configs/best_retrieval_config.yaml",
    "experiments/rag_langgraph_eval/configs/retriever_contract.md",
    "rag/scripts/build_chroma_baseline_index.py",
    "rag/scripts/validate_index.py",
    "rag/retrievers/mock_retriever.py",
    "rag/retrievers/retriever_port.py",
    "rag/eval/retrieval_metrics.py",
    "rag/scripts/validate_source_contract.py",
    "experiments/rag_langgraph_eval/datasets/result_linked_query_cases.json",
    "experiments/rag_langgraph_eval/results/topk_threshold_eval/topk_eval.csv",
]


def main() -> None:
    load_dotenv(dotenv_path=Path(".env"))
    settings = Settings(chroma_collection_documents=COLLECTION_NAME)
    collection = ChromaClientWrapper(settings).get_or_create_document_collection()
    embedding_client = EmbeddingClient(settings)

    ids: list[str] = []
    documents: list[str] = []
    metadatas: list[dict[str, str | int | float | bool]] = []
    missing_paths: list[str] = []

    for relative_path in ARTIFACT_PATHS:
        path = Path(relative_path)
        if not path.exists():
            missing_paths.append(relative_path)
            continue

        text = path.read_text(encoding="utf-8")
        digest = hashlib.sha256(text.encode("utf-8")).hexdigest()[:16]
        chunks = split_text(text)

        for index, chunk in enumerate(chunks, start=1):
            chunk_id = f"dev-artifact::{relative_path}::{index}::{digest}"
            ids.append(chunk_id)
            documents.append(chunk)
            metadatas.append(
                sanitize_metadata(
                    {
                        "document_id": f"dev-artifact::{relative_path}",
                        "document_version_id": digest,
                        "chunk_id": chunk_id,
                        "title": path.name,
                        "document_type": "DEV_ARTIFACT",
                        "category": "rag_integration_artifact",
                        "section_title": f"{path.name} chunk {index}",
                        "page": "",
                        "source_uri": relative_path.replace("\\", "/"),
                        "relative_path": relative_path.replace("\\", "/"),
                        "organization_id": "org-001",
                        "document_status": "PUBLISHED",
                        "embedding_provider": settings.embedding_provider,
                        "embedding_model": settings.embedding_model_name,
                        "collection_name": COLLECTION_NAME,
                        "file_kind": file_kind(path),
                        "chunk_index": index,
                        "chunk_count": len(chunks),
                    }
                )
            )

    if not documents:
        raise RuntimeError("No dev artifacts found to index")

    embeddings = embedding_client.embed_texts(documents)
    collection.upsert(
        ids=ids,
        documents=documents,
        metadatas=metadatas,
        embeddings=embeddings,
    )

    print(
        json.dumps(
            {
                "collection": COLLECTION_NAME,
                "files_requested": len(ARTIFACT_PATHS),
                "files_missing": missing_paths,
                "chunks_upserted": len(documents),
                "collection_count": collection.count(),
                "embedding_provider": settings.embedding_provider,
                "embedding_model": settings.embedding_model_name,
            },
            ensure_ascii=False,
            indent=2,
        )
    )


def split_text(text: str) -> list[str]:
    normalized = text.strip()
    if not normalized:
        return []
    if len(normalized) <= MAX_CHARS:
        return [normalized]

    chunks: list[str] = []
    start = 0
    while start < len(normalized):
        end = min(len(normalized), start + MAX_CHARS)
        chunks.append(normalized[start:end].strip())
        if end == len(normalized):
            break
        start = max(0, end - OVERLAP)

    return [chunk for chunk in chunks if chunk]


def file_kind(path: Path) -> str:
    suffix = path.suffix.lower().lstrip(".") or "text"
    if suffix == "py":
        return "python_source"
    return suffix


def sanitize_metadata(values: dict[str, Any]) -> dict[str, str | int | float | bool]:
    sanitized: dict[str, str | int | float | bool] = {}
    for key, value in values.items():
        if value is None:
            sanitized[key] = ""
        elif isinstance(value, (str, int, float, bool)):
            sanitized[key] = value
        else:
            sanitized[key] = json.dumps(value, ensure_ascii=False)
    return sanitized


if __name__ == "__main__":
    main()
