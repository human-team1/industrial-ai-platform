from __future__ import annotations

import csv
import json
from collections import defaultdict
from pathlib import Path

from rag.chunking.section_chunker import SectionChunker
from rag.loaders.markdown_loader import MarkdownLoader


CORPUS_DIR = "rag/corpus"
DOCUMENT_CATALOG_PATH = "rag/outputs/document_catalog.csv"

RESULT_DIR = Path("experiments/rag_langgraph_eval/results")
DATASET_DIR = Path("experiments/rag_langgraph_eval/datasets")

CHUNK_STATS_PATH = RESULT_DIR / "chunk_stats.csv"
CHUNK_RECORDS_PATH = RESULT_DIR / "chunk_records.jsonl"
MOCK_SOURCES_PATH = DATASET_DIR / "mock_sources_v1.json"

CHUNK_SIZE = 800
CHUNK_OVERLAP = 100


def main() -> None:
    RESULT_DIR.mkdir(parents=True, exist_ok=True)
    DATASET_DIR.mkdir(parents=True, exist_ok=True)

    loader = MarkdownLoader(
        corpus_dir=CORPUS_DIR,
        catalog_path=DOCUMENT_CATALOG_PATH,
    )

    documents = loader.load()

    chunker = SectionChunker(
        chunk_size=CHUNK_SIZE,
        chunk_overlap=CHUNK_OVERLAP,
    )

    chunks = chunker.chunk_documents(documents)

    write_chunk_records(chunks, chunker)
    write_chunk_stats(documents, chunks)
    write_mock_sources(chunks)

    print(f"documents: {len(documents)}")
    print(f"chunks: {len(chunks)}")
    print(f"created: {CHUNK_STATS_PATH}")
    print(f"created: {CHUNK_RECORDS_PATH}")
    print(f"created: {MOCK_SOURCES_PATH}")

    validate_chunks(chunks)

    print("Chunking experiment passed.")


def write_chunk_records(chunks, chunker: SectionChunker) -> None:
    with CHUNK_RECORDS_PATH.open("w", encoding="utf-8") as f:
        for chunk in chunks:
            row = chunker.to_dict(chunk)
            f.write(json.dumps(row, ensure_ascii=False) + "\n")


def write_chunk_stats(documents, chunks) -> None:
    chunks_by_path = defaultdict(list)

    for chunk in chunks:
        chunks_by_path[chunk.relative_path].append(chunk)

    rows = []

    for document in documents:
        document_chunks = chunks_by_path[document.relative_path]
        char_counts = [chunk.char_count for chunk in document_chunks]

        rows.append(
            {
                "relative_path": document.relative_path,
                "title": document.title,
                "document_type": document.document_type,
                "source_group": document.source_group,
                "chunk_size": CHUNK_SIZE,
                "chunk_overlap": CHUNK_OVERLAP,
                "chunk_count": len(document_chunks),
                "min_chunk_chars": min(char_counts) if char_counts else 0,
                "max_chunk_chars": max(char_counts) if char_counts else 0,
                "avg_chunk_chars": round(sum(char_counts) / len(char_counts), 2)
                if char_counts
                else 0,
            }
        )

    with CHUNK_STATS_PATH.open("w", newline="", encoding="utf-8-sig") as f:
        fieldnames = [
            "relative_path",
            "title",
            "document_type",
            "source_group",
            "chunk_size",
            "chunk_overlap",
            "chunk_count",
            "min_chunk_chars",
            "max_chunk_chars",
            "avg_chunk_chars",
        ]

        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def write_mock_sources(chunks) -> None:
    priority_keywords = [
        "FlexLink",
        "Heatmap",
        "OMRON",
        "정상",
        "재검사",
        "일일",
        "작업자",
    ]

    selected = []

    for keyword in priority_keywords:
        for chunk in chunks:
            searchable = f"{chunk.title} {chunk.relative_path} {chunk.section_title or ''}"

            if keyword.lower() in searchable.lower():
                selected.append(chunk)
                break

    unique = []
    seen_ids = set()

    for chunk in selected:
        if chunk.chunk_id in seen_ids:
            continue

        unique.append(chunk)
        seen_ids.add(chunk.chunk_id)

    mock_sources = []

    for rank, chunk in enumerate(unique[:10], start=1):
        mock_sources.append(
            {
                "chunk_id": chunk.chunk_id,
                "document_id": chunk.document_id,
                "document_version_id": chunk.document_version_id,
                "title": chunk.title,
                "document_type": chunk.document_type,
                "section_title": chunk.section_title,
                "page": None,
                "content": chunk.content[:700],
                "score": round(0.95 - (rank * 0.03), 4),
                "rank": rank,
                "source_uri": chunk.source_uri,
            }
        )

    with MOCK_SOURCES_PATH.open("w", encoding="utf-8") as f:
        json.dump(mock_sources, f, ensure_ascii=False, indent=2)


def validate_chunks(chunks) -> None:
    if not chunks:
        raise RuntimeError("No chunks generated.")

    empty_chunks = [chunk for chunk in chunks if not chunk.content.strip()]

    if empty_chunks:
        raise RuntimeError(f"Empty chunks found: {len(empty_chunks)}")

    missing_source_fields = [
        chunk
        for chunk in chunks
        if not chunk.document_id
        or not chunk.document_version_id
        or not chunk.chunk_id
        or not chunk.title
        or not chunk.content
    ]

    if missing_source_fields:
        raise RuntimeError(f"Chunks with missing required fields: {len(missing_source_fields)}")


if __name__ == "__main__":
    main()