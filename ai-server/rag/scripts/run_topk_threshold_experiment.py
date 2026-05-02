from __future__ import annotations

import csv
import itertools
import json
import math
import os
import re
import time
from pathlib import Path

import numpy as np
from dotenv import load_dotenv

from rag.scripts.run_embedding_experiment import (
    CACHE_DIR as EMBEDDING_CACHE_DIR,
    configure_model_cache,
    records_hash,
)
from rag.tracing import RetrievalTracePayload, RetrievalTracer


PROJECT_RESULT_DIR = Path("experiments/rag_langgraph_eval/results")
DATASET_DIR = Path("experiments/rag_langgraph_eval/datasets")
CONFIG_DIR = Path("experiments/rag_langgraph_eval/configs")

CHUNK_RECORDS_PATH = PROJECT_RESULT_DIR / "chunking_matrix" / "C3_chunk_records.jsonl"
QUESTIONS_PATH = DATASET_DIR / "golden_questions_v2.csv"
RESULT_DIR = PROJECT_RESULT_DIR / "topk_threshold_eval"
CACHE_DIR = RESULT_DIR / "cache"

MODEL_NAME = "BAAI/bge-m3"
LOCAL_BGE_SNAPSHOT = (
    EMBEDDING_CACHE_DIR
    / "huggingface"
    / "hub"
    / "models--BAAI--bge-m3"
    / "snapshots"
    / "5617a9f61b028005a4858fdac845db406aefb181"
)
CHUNK_SIZE = 800
CHUNK_OVERLAP = 100


def main() -> None:
    load_dotenv()
    configure_model_cache()

    RESULT_DIR.mkdir(parents=True, exist_ok=True)
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    CONFIG_DIR.mkdir(parents=True, exist_ok=True)

    chunks = load_jsonl(CHUNK_RECORDS_PATH)
    questions = load_questions(QUESTIONS_PATH)
    chunk_texts = [build_chunk_text(chunk) for chunk in chunks]
    query_texts = [build_query_text(row) for row in questions]

    doc_embeddings, query_embeddings = load_or_create_embeddings(
        chunks,
        chunk_texts,
        query_texts,
    )

    configs = build_configs()
    tracer = RetrievalTracer()
    summary_rows = []
    detail_rows = []

    for config in configs:
        summary, detail = run_single_config(
            config=config,
            chunks=chunks,
            questions=questions,
            doc_embeddings=doc_embeddings,
            query_embeddings=query_embeddings,
            tracer=tracer,
        )
        summary_rows.append(summary)
        detail_rows.extend(detail)

    write_csv(RESULT_DIR / "topk_threshold_eval_summary.csv", summary_rows)
    write_csv(RESULT_DIR / "topk_threshold_eval_detail.csv", detail_rows)
    write_query_type_summary(detail_rows)
    best_row = select_best_config(summary_rows)
    write_failed_cases(best_row, detail_rows)
    write_best_config(best_row)
    write_report(summary_rows, best_row, detail_rows, chunks, questions)

    print(f"chunks: {len(chunks)}")
    print(f"documents: {len(set(chunk['document_id'] for chunk in chunks))}")
    print(f"questions: {len(questions)}")
    print(f"configs: {len(configs)}")
    print(f"created: {RESULT_DIR / 'topk_threshold_eval_summary.csv'}")
    print(f"created: {RESULT_DIR / 'topk_threshold_eval_detail.csv'}")
    print(f"created: {RESULT_DIR / 'topk_threshold_by_query_type.csv'}")
    print(f"created: {RESULT_DIR / 'topk_threshold_failed_cases.csv'}")
    print(f"created: {RESULT_DIR / 'topk_threshold_eval_report.md'}")
    print(f"created: {CONFIG_DIR / 'best_retrieval_config.yaml'}")


def load_jsonl(path: Path) -> list[dict]:
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines()]


def load_questions(path: Path) -> list[dict]:
    with path.open("r", encoding="utf-8-sig", newline="") as f:
        return list(csv.DictReader(f))


def build_chunk_text(chunk: dict) -> str:
    return "\n".join(
        [
            f"title: {chunk.get('title') or ''}",
            f"section: {chunk.get('section_title') or ''}",
            f"document_type: {chunk.get('document_type') or ''}",
            f"equipment: {chunk.get('equipment_name') or ''}",
            f"content: {chunk.get('content') or ''}",
        ]
    ).strip()


def build_query_text(row: dict) -> str:
    question = row["question"]

    if row["query_type"] == "result_linked":
        return "\n".join(
            [
                question,
                f"category: {row.get('category', '')}",
                f"equipment: {row.get('equipment_name', '')}",
            ]
        ).strip()

    return question


def load_or_create_embeddings(
    chunks: list[dict],
    chunk_texts: list[str],
    query_texts: list[str],
) -> tuple[np.ndarray, np.ndarray]:
    doc_path = CACHE_DIR / "TOPK_BGE_M3_C3_doc_embeddings.npy"
    query_path = CACHE_DIR / "TOPK_BGE_M3_C3_query_embeddings.npy"

    if doc_path.exists() and query_path.exists():
        return normalize(np.load(doc_path)), normalize(np.load(query_path))

    existing_cache = EMBEDDING_CACHE_DIR / f"E_BGE_M3_{records_hash(chunks)}.json"

    if existing_cache.exists():
        doc_embeddings = np.asarray(
            json.loads(existing_cache.read_text(encoding="utf-8")),
            dtype=np.float32,
        )
    else:
        doc_embeddings = embed_texts(chunk_texts)

    query_embeddings = embed_texts(query_texts)
    doc_embeddings = normalize(doc_embeddings)
    query_embeddings = normalize(query_embeddings)
    np.save(doc_path, doc_embeddings)
    np.save(query_path, query_embeddings)
    return doc_embeddings, query_embeddings


def embed_texts(texts: list[str]) -> np.ndarray:
    from sentence_transformers import SentenceTransformer

    model_path = LOCAL_BGE_SNAPSHOT if LOCAL_BGE_SNAPSHOT.exists() else MODEL_NAME
    model = SentenceTransformer(
        str(model_path),
        cache_folder=str((EMBEDDING_CACHE_DIR / "huggingface").resolve()),
    )
    embeddings = model.encode(
        texts,
        batch_size=16,
        convert_to_numpy=True,
        normalize_embeddings=True,
        show_progress_bar=False,
    )
    return np.asarray(embeddings, dtype=np.float32)


def normalize(matrix: np.ndarray) -> np.ndarray:
    matrix = np.asarray(matrix, dtype=np.float32)
    norms = np.linalg.norm(matrix, axis=1, keepdims=True)
    norms = np.maximum(norms, 1e-12)
    return matrix / norms


def build_configs() -> list[dict]:
    configs = []

    for internal_top_k, min_score, mmr_enabled in itertools.product(
        [3, 5, 10],
        [0.2, 0.3, 0.4],
        [False, True],
    ):
        configs.append(
            {
                "config_id": f"TK{internal_top_k}_S{str(min_score).replace('.', '')}_MMR{int(mmr_enabled)}",
                "internal_top_k": internal_top_k,
                "answer_top_k": min(5, internal_top_k),
                "visible_source_limit": min(3, internal_top_k),
                "min_score": min_score,
                "deduplicate_by_document": True,
                "diversify_by_section": True,
                "mmr_enabled": mmr_enabled,
            }
        )

    return configs


def run_single_config(
    config: dict,
    chunks: list[dict],
    questions: list[dict],
    doc_embeddings: np.ndarray,
    query_embeddings: np.ndarray,
    tracer: RetrievalTracer | None = None,
) -> tuple[dict, list[dict]]:
    started = time.perf_counter()
    detail_rows = []
    raw_candidate_k = max(config["internal_top_k"] * 5, 30)

    for q_index, question in enumerate(questions):
        query_started = time.perf_counter()
        query_embedding = query_embeddings[q_index]
        raw_candidates = vector_search_raw(query_embedding, doc_embeddings, chunks, raw_candidate_k)
        threshold_candidates = [
            candidate
            for candidate in raw_candidates
            if candidate["score"] >= config["min_score"]
        ][: config["internal_top_k"]]

        if config["mmr_enabled"]:
            internal_candidates = mmr_select(
                threshold_candidates,
                query_embedding,
                doc_embeddings,
                config["internal_top_k"],
            )
        else:
            internal_candidates = threshold_candidates

        answer_candidates = apply_source_diversity(
            internal_candidates,
            answer_top_k=config["answer_top_k"],
            deduplicate_by_document=config["deduplicate_by_document"],
            diversify_by_section=config["diversify_by_section"],
            max_per_document=2,
        )
        visible_sources = answer_candidates[: config["visible_source_limit"]]

        first_rank_answer = first_hit_rank(answer_candidates, question)
        first_rank_visible = first_hit_rank(visible_sources, question)
        query_latency_ms = round((time.perf_counter() - query_started) * 1000, 2)
        is_negative = is_negative_question(question)
        no_answer_success = ""

        if is_negative:
            no_answer_success = int(len(answer_candidates) == 0)

        top_1 = answer_candidates[0] if answer_candidates else {}
        trace_retrieval(
            tracer=tracer,
            config=config,
            question=question,
            sources=answer_candidates,
            latency_ms=query_latency_ms,
            expected_doc_hit=first_rank_answer is not None,
        )

        detail_rows.append(
            {
                "config_id": config["config_id"],
                "question_id": question["question_id"],
                "query_type": question["query_type"],
                "question": question["question"],
                "expected_doc_keyword": question.get("expected_doc_keyword", ""),
                "expected_section_keyword": question.get("expected_section_keyword", ""),
                "expected_keywords": question.get("expected_keywords", ""),
                "expected_behavior": question.get("expected_behavior", ""),
                "internal_top_k": config["internal_top_k"],
                "answer_top_k": config["answer_top_k"],
                "visible_source_limit": config["visible_source_limit"],
                "min_score": config["min_score"],
                "deduplicate_by_document": config["deduplicate_by_document"],
                "diversify_by_section": config["diversify_by_section"],
                "mmr_enabled": config["mmr_enabled"],
                "raw_candidate_count": len(raw_candidates),
                "threshold_candidate_count": len(threshold_candidates),
                "answer_source_count": len(answer_candidates),
                "visible_source_count": len(visible_sources),
                "hit_at_1": int(first_rank_answer is not None and first_rank_answer <= 1),
                "hit_at_3": int(first_rank_answer is not None and first_rank_answer <= 3),
                "hit_at_5": int(first_rank_answer is not None and first_rank_answer <= 5),
                "visible_hit_at_3": int(first_rank_visible is not None and first_rank_visible <= 3),
                "mrr": reciprocal_rank(first_rank_answer),
                "first_hit_rank": first_rank_answer or "",
                "no_answer_success": no_answer_success,
                "top_1_title": top_1.get("title", ""),
                "top_1_section": top_1.get("section_title", ""),
                "top_1_score": round(float(top_1.get("score", 0.0)), 6) if top_1 else "",
                "top_5_titles": "|".join(str(item.get("title", "")) for item in answer_candidates[:5]),
                "top_5_chunk_ids": "|".join(str(item.get("chunk_id", "")) for item in answer_candidates[:5]),
                "latency_ms": query_latency_ms,
            }
        )

    summary = summarize_config(config, detail_rows, elapsed_ms=(time.perf_counter() - started) * 1000)
    return summary, detail_rows


def trace_retrieval(
    tracer: RetrievalTracer | None,
    config: dict,
    question: dict,
    sources: list[dict],
    latency_ms: float,
    expected_doc_hit: bool,
) -> None:
    if tracer is None or not tracer.enabled:
        return

    filters = {
        "organization_id": question.get("organization_id", "org-001"),
        "document_status": question.get("document_status", "PUBLISHED"),
        "document_version_policy": "latest_only",
        "equipment_name": question.get("equipment_name") or None,
        "category": question.get("category") or None,
    }
    metadata = tracer.build_metadata(
        query_case_id=question.get("question_id"),
        retrieval_config_id=config["config_id"],
        embedding_model=MODEL_NAME,
        chunk_size=CHUNK_SIZE,
        chunk_overlap=CHUNK_OVERLAP,
        search_mode="vector",
        top_k=config["internal_top_k"],
        expected_doc_hit=expected_doc_hit,
    )
    metadata.update(
        {
            "query_type": question.get("query_type"),
            "min_score": config["min_score"],
            "answer_top_k": config["answer_top_k"],
            "visible_source_limit": config["visible_source_limit"],
            "mmr_enabled": config["mmr_enabled"],
            "source_count": len(sources),
        }
    )
    tracer.trace(
        RetrievalTracePayload(
            query=question["question"],
            filters=filters,
            sources=sources,
            metadata=metadata,
            latency_ms=latency_ms,
        )
    )


def vector_search_raw(
    query_embedding: np.ndarray,
    doc_embeddings: np.ndarray,
    chunks: list[dict],
    candidate_k: int,
) -> list[dict]:
    scores = doc_embeddings @ query_embedding.reshape(-1)
    top_indices = np.argsort(-scores)[:candidate_k]
    candidates = []

    for rank, index in enumerate(top_indices, start=1):
        chunk = chunks[int(index)].copy()
        chunk["_index"] = int(index)
        chunk["score"] = float(scores[int(index)])
        chunk["raw_rank"] = rank
        candidates.append(chunk)

    return candidates


def mmr_select(
    candidates: list[dict],
    query_embedding: np.ndarray,
    doc_embeddings: np.ndarray,
    top_n: int,
    lambda_mult: float = 0.7,
) -> list[dict]:
    if not candidates:
        return []

    selected = []
    remaining = candidates.copy()
    first_pos = int(np.argmax([candidate["score"] for candidate in remaining]))
    selected.append(remaining.pop(first_pos))

    while remaining and len(selected) < top_n:
        selected_embeddings = doc_embeddings[[item["_index"] for item in selected]]
        best_score = -float("inf")
        best_pos = 0

        for pos, candidate in enumerate(remaining):
            candidate_embedding = doc_embeddings[candidate["_index"]]
            relevance = float(candidate_embedding @ query_embedding.reshape(-1))
            diversity_penalty = float(np.max(selected_embeddings @ candidate_embedding.reshape(-1)))
            mmr_score = (lambda_mult * relevance) - ((1 - lambda_mult) * diversity_penalty)

            if mmr_score > best_score:
                best_score = mmr_score
                best_pos = pos

        selected.append(remaining.pop(best_pos))

    return selected


def apply_source_diversity(
    candidates: list[dict],
    answer_top_k: int,
    deduplicate_by_document: bool,
    diversify_by_section: bool,
    max_per_document: int,
) -> list[dict]:
    selected = []
    doc_counts: dict[str, int] = {}
    used_doc_sections = set()

    for candidate in candidates:
        doc_key = get_document_key(candidate)
        section_key = candidate.get("section_title") or "NO_SECTION"
        current_doc_count = doc_counts.get(doc_key, 0)

        if deduplicate_by_document and current_doc_count >= max_per_document:
            continue

        if diversify_by_section:
            doc_section_key = (doc_key, section_key)

            if doc_section_key in used_doc_sections:
                continue

            used_doc_sections.add(doc_section_key)

        selected.append(candidate)
        doc_counts[doc_key] = current_doc_count + 1

        if len(selected) >= answer_top_k:
            break

    return selected


def summarize_config(config: dict, rows: list[dict], elapsed_ms: float) -> dict:
    normal_rows = [row for row in rows if not is_negative_row(row)]
    negative_rows = [row for row in rows if is_negative_row(row)]

    no_answer_values = [
        int(row["no_answer_success"])
        for row in negative_rows
        if row["no_answer_success"] != ""
    ]

    avg_source_count = sum(row["answer_source_count"] for row in rows) / len(rows)
    avg_visible_count = sum(row["visible_source_count"] for row in rows) / len(rows)
    duplicate_doc_ratio = calculate_duplicate_doc_ratio(rows)

    summary = {
        "config_id": config["config_id"],
        "embedding_model": MODEL_NAME,
        "search_mode": "vector",
        "internal_top_k": config["internal_top_k"],
        "answer_top_k": config["answer_top_k"],
        "visible_source_limit": config["visible_source_limit"],
        "min_score": config["min_score"],
        "deduplicate_by_document": config["deduplicate_by_document"],
        "diversify_by_section": config["diversify_by_section"],
        "mmr_enabled": config["mmr_enabled"],
        "question_count": len(rows),
        "normal_question_count": len(normal_rows),
        "no_answer_question_count": len(negative_rows),
        "hit_at_1": mean_metric(normal_rows, "hit_at_1"),
        "hit_at_3": mean_metric(normal_rows, "hit_at_3"),
        "hit_at_5": mean_metric(normal_rows, "hit_at_5"),
        "visible_hit_at_3": mean_metric(normal_rows, "visible_hit_at_3"),
        "mrr": mean_metric(normal_rows, "mrr"),
        "no_answer_accuracy": round(sum(no_answer_values) / len(no_answer_values), 4) if no_answer_values else "",
        "avg_answer_source_count": round(avg_source_count, 2),
        "avg_visible_source_count": round(avg_visible_count, 2),
        "duplicate_doc_ratio": round(duplicate_doc_ratio, 4),
        "elapsed_ms": round(elapsed_ms, 2),
    }
    summary["project_score"] = calculate_project_score(summary)
    return summary


def mean_metric(rows: list[dict], key: str) -> float:
    if not rows:
        return 0.0

    return round(sum(float(row[key]) for row in rows) / len(rows), 4)


def calculate_project_score(row: dict) -> float:
    no_answer_accuracy = float(row["no_answer_accuracy"] or 0)
    project_score = (
        float(row["hit_at_5"]) * 0.35
        + float(row["visible_hit_at_3"]) * 0.25
        + float(row["mrr"]) * 0.20
        + no_answer_accuracy * 0.20
    )
    source_penalty = 0.0 if 2 <= float(row["avg_answer_source_count"]) <= 5 else 0.05
    return round(project_score - source_penalty, 4)


def select_best_config(summary_rows: list[dict]) -> dict:
    return sorted(
        summary_rows,
        key=lambda row: (
            row["project_score"],
            row["hit_at_5"],
            row["visible_hit_at_3"],
            row["no_answer_accuracy"] or 0,
            row["mrr"],
            row["internal_top_k"],
            row["answer_top_k"],
            not row["mmr_enabled"],
            -row["elapsed_ms"],
        ),
        reverse=True,
    )[0]


def write_query_type_summary(detail_rows: list[dict]) -> None:
    grouped: dict[tuple[str, str], list[dict]] = {}

    for row in detail_rows:
        if row["query_type"] in {"no_answer", "out_of_scope"}:
            continue

        grouped.setdefault((row["config_id"], row["query_type"]), []).append(row)

    rows = []

    for (config_id, query_type), group_rows in sorted(grouped.items()):
        rows.append(
            {
                "config_id": config_id,
                "query_type": query_type,
                "question_count": len(group_rows),
                "hit_at_1": mean_metric(group_rows, "hit_at_1"),
                "hit_at_3": mean_metric(group_rows, "hit_at_3"),
                "hit_at_5": mean_metric(group_rows, "hit_at_5"),
                "visible_hit_at_3": mean_metric(group_rows, "visible_hit_at_3"),
                "mrr": mean_metric(group_rows, "mrr"),
            }
        )

    write_csv(RESULT_DIR / "topk_threshold_by_query_type.csv", rows)


def write_failed_cases(best_row: dict, detail_rows: list[dict]) -> None:
    failed_rows = []

    for row in detail_rows:
        if row["config_id"] != best_row["config_id"]:
            continue

        if is_negative_row(row) and row["no_answer_success"] == 0:
            failed_rows.append(row)
        elif not is_negative_row(row) and row["hit_at_5"] == 0:
            failed_rows.append(row)

    if failed_rows:
        write_csv(RESULT_DIR / "topk_threshold_failed_cases.csv", failed_rows)
    else:
        (RESULT_DIR / "topk_threshold_failed_cases.csv").write_text(
            "config_id,question_id,query_type,question\n",
            encoding="utf-8-sig",
        )


def write_best_config(best_row: dict) -> None:
    yaml_text = f"""retrieval_config_id: {best_row["config_id"]}
embedding_model: {MODEL_NAME}
search_mode: vector
collection_name: industrial_rag_chunks_a_v1
chunk_size: {CHUNK_SIZE}
chunk_overlap: {CHUNK_OVERLAP}

internal_top_k: {int(best_row["internal_top_k"])}
answer_top_k: {int(best_row["answer_top_k"])}
visible_source_limit: {int(best_row["visible_source_limit"])}
min_score: {float(best_row["min_score"])}

document_status_filter: PUBLISHED
document_version_policy: latest_only
organization_filter_required: true

deduplicate_by_document: {str(bool(best_row["deduplicate_by_document"])).lower()}
diversify_by_section: {str(bool(best_row["diversify_by_section"])).lower()}
mmr_enabled: {str(bool(best_row["mmr_enabled"])).lower()}

return_fields:
  - chunk_id
  - document_id
  - document_version_id
  - title
  - document_type
  - category
  - equipment_name
  - section_title
  - page
  - content
  - score
  - rank
  - source_uri
"""
    (CONFIG_DIR / "best_retrieval_config.yaml").write_text(yaml_text, encoding="utf-8")


def write_report(
    summary_rows: list[dict],
    best_row: dict,
    detail_rows: list[dict],
    chunks: list[dict],
    questions: list[dict],
) -> None:
    failed_count = sum(
        1
        for row in detail_rows
        if row["config_id"] == best_row["config_id"]
        and ((is_negative_row(row) and row["no_answer_success"] == 0) or (not is_negative_row(row) and row["hit_at_5"] == 0))
    )
    lines = [
        "# Top-k / Threshold Retrieval Evaluation Report",
        "",
        "## Experiment Setup",
        "",
        f"- documents: `{len(set(chunk['document_id'] for chunk in chunks))}`",
        f"- chunks: `{len(chunks)}`",
        f"- questions: `{len(questions)}`",
        f"- embedding_model: `{MODEL_NAME}`",
        f"- chunk_config: `chunk_size={CHUNK_SIZE}`, `chunk_overlap={CHUNK_OVERLAP}`",
        "- search_mode: `vector`",
        "- internal_top_k candidates: `3 / 5 / 10`",
        "- min_score candidates: `0.2 / 0.3 / 0.4`",
        "- source diversity: `deduplicate_by_document=true`, `diversify_by_section=true`",
        "- MMR: `false / true`",
        "",
        "## Best Config",
        "",
        f"- config_id: `{best_row['config_id']}`",
        f"- internal_top_k: `{best_row['internal_top_k']}`",
        f"- answer_top_k: `{best_row['answer_top_k']}`",
        f"- visible_source_limit: `{best_row['visible_source_limit']}`",
        f"- min_score: `{best_row['min_score']}`",
        f"- mmr_enabled: `{best_row['mmr_enabled']}`",
        f"- hit_at_5: `{best_row['hit_at_5']}`",
        f"- visible_hit_at_3: `{best_row['visible_hit_at_3']}`",
        f"- mrr: `{best_row['mrr']}`",
        f"- no_answer_accuracy: `{best_row['no_answer_accuracy']}`",
        f"- avg_answer_source_count: `{best_row['avg_answer_source_count']}`",
        f"- failed_cases: `{failed_count}`",
        "",
        "## Summary Table",
        "",
        "| Config | internal_top_k | min_score | MMR | hit@1 | hit@3 | hit@5 | visible_hit@3 | MRR | no_answer_acc | avg_sources | dup_doc_ratio | elapsed_ms | project_score |",
        "|---|---:|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|",
    ]

    for row in sorted(summary_rows, key=lambda item: item["config_id"]):
        lines.append(
            f"| {row['config_id']} | {row['internal_top_k']} | {row['min_score']} | {row['mmr_enabled']} | "
            f"{row['hit_at_1']} | {row['hit_at_3']} | {row['hit_at_5']} | {row['visible_hit_at_3']} | "
            f"{row['mrr']} | {row['no_answer_accuracy']} | {row['avg_answer_source_count']} | "
            f"{row['duplicate_doc_ratio']} | {row['elapsed_ms']} | {row['project_score']} |"
        )

    lines.extend(
        [
            "",
            "## Conclusion",
            "",
            "본 실험은 BAAI/bge-m3 embedding과 C3 chunk 설정을 고정하고 internal_top_k, min_score, MMR 적용 여부에 따른 검색 품질을 비교했다.",
            "최종 설정은 hit@5, visible_hit@3, MRR, no_answer_accuracy를 함께 고려해 선정했다.",
            "관련 문서가 없는 질문에서 source를 비워야 하는 정책 때문에 no_answer_accuracy를 선택 기준에 포함했다.",
            "",
        ]
    )
    (RESULT_DIR / "topk_threshold_eval_report.md").write_text("\n".join(lines), encoding="utf-8")


def write_csv(path: Path, rows: list[dict]) -> None:
    with path.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)


def is_expected_hit(chunk: dict, question: dict) -> bool:
    expected_doc = (question.get("expected_doc_keyword") or "").strip()
    expected_section = (question.get("expected_section_keyword") or "").strip()

    if expected_doc in {"NO_ANSWER", "OUT_OF_SCOPE"}:
        return False

    text = searchable_text(chunk)
    doc_hit = bool(expected_doc and expected_doc.lower() in text)
    section_hit = bool(expected_section and expected_section.lower() in text)
    keyword_hit = any(keyword in text for keyword in split_keywords(question.get("expected_keywords") or ""))
    return doc_hit or section_hit or keyword_hit


def first_hit_rank(results: list[dict], question: dict) -> int | None:
    for rank, chunk in enumerate(results, start=1):
        if is_expected_hit(chunk, question):
            return rank

    return None


def split_keywords(value: str) -> list[str]:
    raw = str(value).replace("|", ";")
    return [item.strip().lower() for item in raw.split(";") if item.strip()]


def searchable_text(chunk: dict) -> str:
    return " ".join(
        str(value)
        for value in [
            chunk.get("title") or "",
            chunk.get("section_title") or "",
            chunk.get("content") or "",
            chunk.get("source_uri") or "",
            chunk.get("relative_path") or "",
            chunk.get("document_type") or "",
            chunk.get("equipment_name") or "",
        ]
    ).lower()


def reciprocal_rank(rank: int | None) -> float:
    return round(1.0 / rank, 4) if rank else 0.0


def is_negative_question(question: dict) -> bool:
    return question["query_type"] in {"no_answer", "out_of_scope"} or question.get("expected_behavior") in {
        "no_retrieval_result",
        "out_of_scope_response",
    }


def is_negative_row(row: dict) -> bool:
    return row["query_type"] in {"no_answer", "out_of_scope"} or row.get("expected_behavior") in {
        "no_retrieval_result",
        "out_of_scope_response",
    }


def get_document_key(chunk: dict) -> str:
    return chunk.get("document_id") or chunk.get("relative_path") or chunk.get("source_uri") or chunk.get("title") or "unknown"


def calculate_duplicate_doc_ratio(rows: list[dict]) -> float:
    ratios = []

    for row in rows:
        chunk_ids = [chunk_id for chunk_id in row["top_5_chunk_ids"].split("|") if chunk_id]

        if len(chunk_ids) <= 1:
            ratios.append(0.0)
            continue

        doc_ids = [chunk_id.split("::")[0] for chunk_id in chunk_ids]
        ratios.append(1 - (len(set(doc_ids)) / len(doc_ids)))

    return sum(ratios) / len(ratios) if ratios else 0.0


if __name__ == "__main__":
    main()
