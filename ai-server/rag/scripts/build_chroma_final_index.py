from __future__ import annotations

import json
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from dotenv import load_dotenv


sys.path.append(str(Path(__file__).resolve().parents[2]))

from config.settings import Settings  # noqa: E402
from infrastructure.chroma_client import ChromaClientWrapper  # noqa: E402
from infrastructure.embedding_client import EmbeddingClient  # noqa: E402


CHUNK_RECORDS_PATH = Path(
    "experiments/rag_langgraph_eval/results/chunking_matrix/C3_chunk_records.jsonl"
)
INDEX_BUILD_LOG_PATH = Path(
    "experiments/rag_langgraph_eval/results/retrieval_eval/index_build_log.jsonl"
)
INDEX_MANIFEST_PATH = Path(
    "experiments/rag_langgraph_eval/results/retrieval_eval/chroma_index_manifest.json"
)


def main() -> None:
    load_dotenv()
    settings = Settings()

    chunks = load_jsonl(CHUNK_RECORDS_PATH)
    if not chunks:
        raise RuntimeError(f"No chunk records found: {CHUNK_RECORDS_PATH}")

    embedding_client = EmbeddingClient(settings)
    chroma_client = ChromaClientWrapper(settings)
    collection = chroma_client.get_or_create_document_collection()

    ids = [str(chunk["chunk_id"]) for chunk in chunks]
    documents = [str(chunk.get("content") or "") for chunk in chunks]
    metadatas = [
        build_metadata(
            chunk=chunk,
            settings=settings,
            vector_ref=str(chunk["chunk_id"]),
        )
        for chunk in chunks
    ]
    embeddings = embedding_client.embed_texts(documents)

    collection.upsert(
        ids=ids,
        documents=documents,
        metadatas=metadatas,
        embeddings=embeddings,
    )

    count = collection.count()
    event = {
        "event": "chroma_final_index_built",
        "retrieval_config_id": settings.retrieval_config_id,
        "collection_name": settings.chroma_collection_documents,
        "embedding_provider": settings.embedding_provider,
        "embedding_model": settings.embedding_model_name,
        "chunk_size": settings.rag_chunk_size,
        "chunk_overlap": settings.rag_chunk_overlap,
        "search_mode": settings.rag_search_type,
        "internal_top_k": settings.retrieval_internal_top_k,
        "answer_top_k": settings.retrieval_answer_top_k,
        "min_score": settings.rag_min_score,
        "document_status_filter": settings.rag_default_document_status,
        "chunk_count": len(chunks),
        "collection_count": count,
        "source_path": str(CHUNK_RECORDS_PATH),
    }
    log_event(event)
    write_manifest(event)

    print(f"collection: {settings.chroma_collection_documents}")
    print(f"chunks_indexed: {len(chunks)}")
    print(f"collection_count: {count}")
    print(f"log: {INDEX_BUILD_LOG_PATH}")
    print(f"manifest: {INDEX_MANIFEST_PATH}")


def load_jsonl(path: Path) -> list[dict[str, Any]]:
    return [
        json.loads(line)
        for line in path.read_text(encoding="utf-8").splitlines()
        if line.strip()
    ]


def build_metadata(
    chunk: dict[str, Any],
    settings: Settings,
    vector_ref: str,
) -> dict[str, str | int | float | bool]:
    organization_id = normalize_organization_id(chunk.get("organization_id"))

    metadata: dict[str, Any] = {
        "retrieval_config_id": settings.retrieval_config_id,
        "retrieval_frozen_at": settings.retrieval_frozen_at,
        "document_id": chunk.get("document_id"),
        "document_version_id": chunk.get("document_version_id"),
        "chunk_id": chunk.get("chunk_id"),
        "title": chunk.get("title"),
        "document_type": chunk.get("document_type"),
        "category": chunk.get("category"),
        "equipment_name": chunk.get("equipment_name"),
        "section_title": chunk.get("section_title") or "section_unspecified",
        "section_level": chunk.get("section_level"),
        "page": chunk.get("page") or "",
        "sequence_no": chunk.get("sequence_no"),
        "char_count": chunk.get("char_count"),
        "source_group": chunk.get("source_group"),
        "relative_path": chunk.get("relative_path"),
        "source_uri": chunk.get("source_uri"),
        "organization_id": organization_id,
        "document_status": chunk.get("document_status")
        or settings.rag_default_document_status,
        "document_version_policy": "latest_only",
        "chunk_size": settings.rag_chunk_size,
        "chunk_overlap": settings.rag_chunk_overlap,
        "embedding_provider": settings.embedding_provider,
        "embedding_model": settings.embedding_model_name,
        "vector_ref": vector_ref,
        "created_at": datetime.now(timezone.utc).isoformat(),
    }

    return sanitize_metadata(metadata)


def normalize_organization_id(value: Any) -> str:
    if value is None:
        raise ValueError("organization_id is required for chunk metadata")

    normalized = str(value).strip()
    if not normalized:
        raise ValueError("organization_id is required for chunk metadata")

    if normalized.lower().startswith("org-"):
        suffix = normalized[4:]
        if suffix.isdigit() and int(suffix) > 0:
            return suffix
        raise ValueError(f"invalid organization_id: {value}")

    if normalized.isdigit() and int(normalized) > 0:
        return normalized

    if normalized.endswith(".0"):
        integer_part = normalized[:-2]
        if integer_part.isdigit() and int(integer_part) > 0:
            return integer_part

    raise ValueError(f"invalid organization_id: {value}")


def sanitize_metadata(values: dict[str, Any]) -> dict[str, str | int | float | bool]:
    sanitized: dict[str, str | int | float | bool] = {}
    for key, value in values.items():
        if value is None:
            sanitized[key] = ""
        elif isinstance(value, (str, int, float, bool)):
            sanitized[key] = value
        else:
            sanitized[key] = str(value)
    return sanitized


def log_event(payload: dict[str, Any]) -> None:
    INDEX_BUILD_LOG_PATH.parent.mkdir(parents=True, exist_ok=True)
    row = {
        "created_at": datetime.now(timezone.utc).isoformat(),
        **payload,
    }
    with INDEX_BUILD_LOG_PATH.open("a", encoding="utf-8") as f:
        f.write(json.dumps(row, ensure_ascii=False) + "\n")


def write_manifest(payload: dict[str, Any]) -> None:
    INDEX_MANIFEST_PATH.parent.mkdir(parents=True, exist_ok=True)
    manifest = {
        "created_at": datetime.now(timezone.utc).isoformat(),
        "storage_decision": {
            "chroma_db": "stores embeddings, documents, and search metadata for SourceChunk retrieval",
            "mariadb": "stores DOCUMENT, DOCUMENT_VERSION, CHUNK, VECTOR_INDEX metadata and indexing job history",
            "minio": "stores original document files",
        },
        "mariadb_mapping": {
            "DOCUMENT": ["document_id", "organization_id", "owner_user_id", "title", "document_type", "current_status"],
            "DOCUMENT_VERSION": ["document_version_id", "document_id", "version_no", "file_id", "file_hash", "indexing_status", "indexed_chunk_count", "indexed_at"],
            "CHUNK": ["chunk_id", "document_version_id", "sequence_no", "content"],
            "VECTOR_INDEX": ["vector_id", "chunk_id", "embedding_model", "vector_ref"],
        },
        "chroma_payload": {
            "id": "chunk_id",
            "document": "content",
            "embedding": "EmbeddingClient.embed_texts(content)",
            "metadata": [
                "retrieval_config_id",
                "document_id",
                "document_version_id",
                "chunk_id",
                "title",
                "document_type",
                "category",
                "equipment_name",
                "section_title",
                "page",
                "organization_id",
                "document_status",
                "chunk_size",
                "chunk_overlap",
                "embedding_model",
                "vector_ref",
            ],
        },
        "build": payload,
    }
    INDEX_MANIFEST_PATH.write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


if __name__ == "__main__":
    main()
