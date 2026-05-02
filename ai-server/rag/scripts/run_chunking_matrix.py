from __future__ import annotations

import csv
import json
from collections import defaultdict
from dataclasses import asdict
from pathlib import Path
from statistics import mean, median

from rag.chunking.section_chunker import ChunkRecord, SectionChunker
from rag.loaders.markdown_loader import LoadedDocument, MarkdownLoader


CORPUS_DIR = "rag/corpus"
DOCUMENT_CATALOG_PATH = "rag/outputs/document_catalog.csv"

RESULT_DIR = Path("experiments/rag_langgraph_eval/results")
MATRIX_DIR = RESULT_DIR / "chunking_matrix"


EXPERIMENTS = [
    {
        "experiment_id": "C1",
        "strategy": "section_sliding",
        "chunk_size": 300,
        "chunk_overlap": 50,
        "purpose": "짧은 질의에 빠르게 반응하는지",
    },
    {
        "experiment_id": "C2",
        "strategy": "section_sliding",
        "chunk_size": 500,
        "chunk_overlap": 100,
        "purpose": "균형형 baseline",
    },
    {
        "experiment_id": "C3",
        "strategy": "section_sliding",
        "chunk_size": 800,
        "chunk_overlap": 100,
        "purpose": "매뉴얼 절차 단위 보존",
    },
    {
        "experiment_id": "C4",
        "strategy": "section_sliding",
        "chunk_size": 1000,
        "chunk_overlap": 150,
        "purpose": "긴 대응 절차 보존",
    },
    {
        "experiment_id": "C5",
        "strategy": "section_based",
        "chunk_size": None,
        "chunk_overlap": None,
        "purpose": "제목/섹션 단위 검색 품질 확인",
    },
    {
        "experiment_id": "C6",
        "strategy": "heading_body_hybrid",
        "chunk_size": 800,
        "chunk_overlap": 100,
        "purpose": "제목은 유지하고 본문만 token split",
    },
]


def main() -> None:
    MATRIX_DIR.mkdir(parents=True, exist_ok=True)

    documents = MarkdownLoader(
        corpus_dir=CORPUS_DIR,
        catalog_path=DOCUMENT_CATALOG_PATH,
    ).load()

    summary_rows = []

    for experiment in EXPERIMENTS:
        chunks = run_experiment(documents, experiment)
        validate_chunks(chunks, experiment)
        write_chunk_records(chunks, experiment)
        write_document_stats(documents, chunks, experiment)
        summary_rows.append(build_summary_row(documents, chunks, experiment))

    write_summary(summary_rows)
    write_report(summary_rows)

    print(f"documents: {len(documents)}")
    print(f"experiments: {len(EXPERIMENTS)}")
    print(f"created: {MATRIX_DIR / 'chunking_experiment_summary.csv'}")
    print(f"created: {MATRIX_DIR / 'chunking_experiment_report.md'}")
    print("Chunking matrix experiment passed.")


def run_experiment(
    documents: list[LoadedDocument],
    experiment: dict,
) -> list[ChunkRecord]:
    strategy = experiment["strategy"]

    if strategy == "section_sliding":
        chunker = SectionChunker(
            chunk_size=experiment["chunk_size"],
            chunk_overlap=experiment["chunk_overlap"],
        )
        return chunker.chunk_documents(documents)

    if strategy == "section_based":
        return chunk_section_based(documents)

    if strategy == "heading_body_hybrid":
        return chunk_heading_body_hybrid(
            documents,
            chunk_size=experiment["chunk_size"],
            chunk_overlap=experiment["chunk_overlap"],
        )

    raise ValueError(f"Unsupported chunking strategy: {strategy}")


def chunk_section_based(documents: list[LoadedDocument]) -> list[ChunkRecord]:
    helper = SectionChunker(chunk_size=1, chunk_overlap=0)
    chunks: list[ChunkRecord] = []

    for document in documents:
        document_id = helper._make_document_id(document.relative_path)
        document_version_id = f"{document_id}-v1"
        sequence_no = 1

        for section in helper._split_sections(document.content):
            content = section["content"].strip()

            if not content:
                continue

            chunks.append(
                ChunkRecord(
                    chunk_id=f"{document_id}::chunk_{sequence_no:04d}",
                    document_id=document_id,
                    document_version_id=document_version_id,
                    title=document.title,
                    document_type=document.document_type,
                    source_group=document.source_group,
                    relative_path=document.relative_path,
                    section_title=section["title"],
                    section_level=section["level"],
                    sequence_no=sequence_no,
                    content=content,
                    char_count=len(content),
                    source_uri=document.source_uri,
                )
            )
            sequence_no += 1

    return chunks


def chunk_heading_body_hybrid(
    documents: list[LoadedDocument],
    chunk_size: int,
    chunk_overlap: int,
) -> list[ChunkRecord]:
    helper = SectionChunker(chunk_size=chunk_size, chunk_overlap=chunk_overlap)
    chunks: list[ChunkRecord] = []

    for document in documents:
        document_id = helper._make_document_id(document.relative_path)
        document_version_id = f"{document_id}-v1"
        sequence_no = 1

        for section in helper._split_sections(document.content):
            section_title = section["title"]
            section_level = section["level"]
            section_content = section["content"].strip()

            if not section_content:
                continue

            heading_prefix = build_heading_prefix(section_title, section_level)
            body = remove_repeated_heading(section_content, section_title)
            body_size = max(1, chunk_size - len(heading_prefix))
            body_overlap = min(chunk_overlap, max(0, body_size - 1))
            split_bodies = split_text(body, body_size, body_overlap)

            for split_body in split_bodies:
                content = f"{heading_prefix}{split_body}".strip()
                chunks.append(
                    ChunkRecord(
                        chunk_id=f"{document_id}::chunk_{sequence_no:04d}",
                        document_id=document_id,
                        document_version_id=document_version_id,
                        title=document.title,
                        document_type=document.document_type,
                        source_group=document.source_group,
                        relative_path=document.relative_path,
                        section_title=section_title,
                        section_level=section_level,
                        sequence_no=sequence_no,
                        content=content,
                        char_count=len(content),
                        source_uri=document.source_uri,
                    )
                )
                sequence_no += 1

    return chunks


def build_heading_prefix(section_title: str | None, section_level: int | None) -> str:
    if not section_title:
        return ""

    level = section_level or 2
    return f"{'#' * level} {section_title}\n\n"


def remove_repeated_heading(content: str, section_title: str | None) -> str:
    if not section_title:
        return content

    lines = content.splitlines()

    if lines and lines[0].lstrip("#").strip() == section_title:
        return "\n".join(lines[1:]).strip()

    return content


def split_text(text: str, chunk_size: int, chunk_overlap: int) -> list[str]:
    text = text.strip()

    if len(text) <= chunk_size:
        return [text]

    chunks: list[str] = []
    start = 0

    while start < len(text):
        end = start + chunk_size
        chunk = text[start:end].strip()

        if chunk:
            chunks.append(chunk)

        if end >= len(text):
            break

        start = end - chunk_overlap

    return chunks


def write_chunk_records(chunks: list[ChunkRecord], experiment: dict) -> None:
    path = MATRIX_DIR / f"{experiment['experiment_id']}_chunk_records.jsonl"

    with path.open("w", encoding="utf-8") as f:
        for chunk in chunks:
            f.write(json.dumps(asdict(chunk), ensure_ascii=False) + "\n")


def write_document_stats(
    documents: list[LoadedDocument],
    chunks: list[ChunkRecord],
    experiment: dict,
) -> None:
    path = MATRIX_DIR / f"{experiment['experiment_id']}_chunk_stats.csv"
    chunks_by_path = defaultdict(list)

    for chunk in chunks:
        chunks_by_path[chunk.relative_path].append(chunk)

    fieldnames = [
        "experiment_id",
        "strategy",
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

    rows = []

    for document in documents:
        document_chunks = chunks_by_path[document.relative_path]
        char_counts = [chunk.char_count for chunk in document_chunks]
        rows.append(
            {
                "experiment_id": experiment["experiment_id"],
                "strategy": experiment["strategy"],
                "relative_path": document.relative_path,
                "title": document.title,
                "document_type": document.document_type,
                "source_group": document.source_group,
                "chunk_size": experiment["chunk_size"] or "section-based",
                "chunk_overlap": experiment["chunk_overlap"] or "section-based",
                "chunk_count": len(document_chunks),
                "min_chunk_chars": min(char_counts) if char_counts else 0,
                "max_chunk_chars": max(char_counts) if char_counts else 0,
                "avg_chunk_chars": round(mean(char_counts), 2) if char_counts else 0,
            }
        )

    with path.open("w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def build_summary_row(
    documents: list[LoadedDocument],
    chunks: list[ChunkRecord],
    experiment: dict,
) -> dict:
    char_counts = [chunk.char_count for chunk in chunks]
    over_limit_count = 0

    if experiment["chunk_size"]:
        over_limit_count = sum(count > experiment["chunk_size"] for count in char_counts)

    return {
        "experiment_id": experiment["experiment_id"],
        "strategy": experiment["strategy"],
        "purpose": experiment["purpose"],
        "document_count": len(documents),
        "chunk_size": experiment["chunk_size"] or "section-based",
        "chunk_overlap": experiment["chunk_overlap"] or "section-based",
        "chunk_count": len(chunks),
        "min_chunk_chars": min(char_counts),
        "p50_chunk_chars": int(median(char_counts)),
        "avg_chunk_chars": round(mean(char_counts), 2),
        "max_chunk_chars": max(char_counts),
        "tiny_lt20_count": sum(count < 20 for count in char_counts),
        "over_limit_count": over_limit_count,
    }


def write_summary(rows: list[dict]) -> None:
    path = MATRIX_DIR / "chunking_experiment_summary.csv"

    with path.open("w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)


def write_report(rows: list[dict]) -> None:
    path = MATRIX_DIR / "chunking_experiment_report.md"
    lines = [
        "# Chunking Experiment Report",
        "",
        "## Summary",
        "",
        "| ID | Strategy | Size | Overlap | Chunks | Min | P50 | Avg | Max | <20 | Over Limit | Purpose |",
        "| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |",
    ]

    for row in rows:
        lines.append(
            "| {experiment_id} | {strategy} | {chunk_size} | {chunk_overlap} | "
            "{chunk_count} | {min_chunk_chars} | {p50_chunk_chars} | "
            "{avg_chunk_chars} | {max_chunk_chars} | {tiny_lt20_count} | "
            "{over_limit_count} | {purpose} |".format(
                **row
            )
        )

    lines.extend(
        [
            "",
            "## Notes",
            "",
            "- `chunk_size`는 목표 길이가 아니라 최대 허용 길이다.",
            "- C5는 섹션 자체를 보존하므로 `over_limit_count`를 적용하지 않는다.",
            "- C6는 섹션 제목을 각 chunk에 유지하고 본문만 분할한다.",
            "- `<20`은 검색 노이즈 후보 chunk 수다. Chroma 적재 전 필터링 여부를 결정한다.",
            "- 다음 단계에서는 각 실험 산출물을 Chroma에 적재한 뒤 golden question 기준 hit@k/MRR을 비교한다.",
            "",
        ]
    )

    path.write_text("\n".join(lines), encoding="utf-8")


def validate_chunks(chunks: list[ChunkRecord], experiment: dict) -> None:
    if not chunks:
        raise RuntimeError(f"{experiment['experiment_id']}: no chunks generated")

    required_missing = [
        chunk
        for chunk in chunks
        if not chunk.document_id
        or not chunk.document_version_id
        or not chunk.chunk_id
        or not chunk.title
        or not chunk.content.strip()
        or not chunk.source_uri
    ]

    if required_missing:
        raise RuntimeError(
            f"{experiment['experiment_id']}: chunks with missing required fields: "
            f"{len(required_missing)}"
        )

    if experiment["chunk_size"] and experiment["strategy"] != "section_based":
        over_limit_count = sum(
            chunk.char_count > experiment["chunk_size"] for chunk in chunks
        )

        if over_limit_count:
            raise RuntimeError(
                f"{experiment['experiment_id']}: chunks over size limit: "
                f"{over_limit_count}"
            )


if __name__ == "__main__":
    main()
