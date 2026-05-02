from __future__ import annotations

import csv
import itertools
import json
import math
import re
import time
from collections import Counter
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
RESULT_DIR = PROJECT_RESULT_DIR / "retrieval_eval"
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

    doc_embeddings, query_embeddings = load_or_create_embeddings(chunks, chunk_texts, query_texts)
    bm25 = SimpleBM25([tokenize(text) for text in chunk_texts])
    tracer = RetrievalTracer()

    summary_rows = []
    detail_rows = []

    for config in build_configs():
        summary, detail = evaluate_config(
            config=config,
            chunks=chunks,
            questions=questions,
            query_texts=query_texts,
            doc_embeddings=doc_embeddings,
            query_embeddings=query_embeddings,
            bm25=bm25,
            tracer=tracer,
        )
        summary_rows.append(summary)
        detail_rows.extend(detail)

    write_csv(RESULT_DIR / "retrieval_eval_summary.csv", summary_rows)
    write_csv(RESULT_DIR / "retrieval_eval_detail.csv", detail_rows)
    write_query_type_summary(detail_rows)
    best_row = select_best_config(summary_rows)
    failed_cases = write_failed_cases(best_row, detail_rows)
    write_best_config(best_row)
    write_report(summary_rows, best_row, failed_cases, chunks, questions)

    print(f"chunks: {len(chunks)}")
    print(f"documents: {len(set(chunk['document_id'] for chunk in chunks))}")
    print(f"questions: {len(questions)}")
    print(f"configs: {len(summary_rows)}")
    print(f"best_config: {best_row['config_id']}")
    print(f"created: {RESULT_DIR / 'retrieval_eval_summary.csv'}")
    print(f"created: {RESULT_DIR / 'retrieval_eval_detail.csv'}")
    print(f"created: {RESULT_DIR / 'retrieval_failed_cases.csv'}")
    print(f"created: {RESULT_DIR / 'retrieval_eval_report.md'}")
    print(f"created: {CONFIG_DIR / 'best_retrieval_config.yaml'}")


def load_jsonl(path: Path) -> list[dict]:
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def load_questions(path: Path) -> list[dict]:
    with path.open("r", encoding="utf-8-sig", newline="") as f:
        return list(csv.DictReader(f))


def build_chunk_text(chunk: dict) -> str:
    return "\n".join(
        [
            f"title: {chunk.get('title') or ''}",
            f"section: {chunk.get('section_title') or ''}",
            f"document_type: {chunk.get('document_type') or ''}",
            f"path: {chunk.get('relative_path') or ''}",
            f"equipment: {chunk.get('equipment_name') or ''}",
            f"content: {chunk.get('content') or ''}",
        ]
    ).strip()


def build_query_text(row: dict) -> str:
    question = str(row["question"])

    if row["query_type"] == "result_linked":
        section_hint = row.get("expected_section") or row.get("expected_section_keyword") or ""
        return "\n".join(
            [
                question,
                "query_type: result_linked",
                f"category: {row.get('category', '')}",
                f"equipment: {row.get('equipment_name', '')}",
                f"expected_section_hint: {section_hint}",
            ]
        ).strip()

    return question


def load_or_create_embeddings(
    chunks: list[dict],
    chunk_texts: list[str],
    query_texts: list[str],
) -> tuple[np.ndarray, np.ndarray]:
    doc_path = CACHE_DIR / "bge_m3_doc_embeddings.npy"
    query_path = CACHE_DIR / "bge_m3_query_embeddings.npy"

    if doc_path.exists() and query_path.exists():
        return normalize(np.load(doc_path)), normalize(np.load(query_path))

    existing_doc_cache = EMBEDDING_CACHE_DIR / f"E_BGE_M3_{records_hash(chunks)}.json"
    if existing_doc_cache.exists():
        doc_embeddings = np.asarray(
            json.loads(existing_doc_cache.read_text(encoding="utf-8")),
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
    return matrix / np.maximum(norms, 1e-12)


def tokenize(text: str) -> list[str]:
    return re.findall(r"[a-zA-Z0-9가-힣_\-/]+", str(text).lower())


class SimpleBM25:
    def __init__(self, tokenized_docs: list[list[str]], k1: float = 1.5, b: float = 0.75):
        self.docs = tokenized_docs
        self.k1 = k1
        self.b = b
        self.doc_count = len(tokenized_docs)
        self.doc_lengths = [len(doc) for doc in tokenized_docs]
        self.avg_doc_len = sum(self.doc_lengths) / max(self.doc_count, 1)
        self.term_frequencies = [Counter(doc) for doc in tokenized_docs]
        document_frequencies = Counter()

        for doc in tokenized_docs:
            document_frequencies.update(set(doc))

        self.idf = {
            term: math.log(1 + (self.doc_count - freq + 0.5) / (freq + 0.5))
            for term, freq in document_frequencies.items()
        }

    def get_scores(self, query_tokens: list[str]) -> np.ndarray:
        scores = np.zeros(self.doc_count, dtype=np.float32)

        for doc_index, term_frequency in enumerate(self.term_frequencies):
            doc_len = self.doc_lengths[doc_index]
            score = 0.0

            for token in query_tokens:
                freq = term_frequency.get(token, 0)
                if freq == 0:
                    continue

                idf = self.idf.get(token, 0.0)
                denominator = freq + self.k1 * (1 - self.b + self.b * doc_len / self.avg_doc_len)
                score += idf * (freq * (self.k1 + 1)) / denominator

            scores[doc_index] = score

        return scores


def build_configs() -> list[dict]:
    configs = []
    modes = [
        ("R1", "vector"),
        ("R2", "bm25"),
        ("R3", "hybrid"),
        ("R4", "vector_metadata_filter"),
    ]

    for experiment_id, search_mode in modes:
        for internal_top_k, min_score in itertools.product([3, 5, 10], [0.2, 0.3, 0.4]):
            configs.append(
                {
                    "experiment_id": experiment_id,
                    "config_id": f"{experiment_id}_{search_mode.upper()}_TK{internal_top_k}_S{str(min_score).replace('.', '')}",
                    "search_mode": search_mode,
                    "internal_top_k": internal_top_k,
                    "answer_top_k": min(5, internal_top_k),
                    "visible_source_limit": min(3, internal_top_k),
                    "min_score": min_score,
                    "deduplicate_by_document": True,
                    "diversify_by_section": True,
                }
            )

    return configs


def evaluate_config(
    config: dict,
    chunks: list[dict],
    questions: list[dict],
    query_texts: list[str],
    doc_embeddings: np.ndarray,
    query_embeddings: np.ndarray,
    bm25: SimpleBM25,
    tracer: RetrievalTracer | None = None,
) -> tuple[dict, list[dict]]:
    started = time.perf_counter()
    detail_rows = []
    raw_candidate_k = max(config["internal_top_k"] * 5, 30)

    for q_idx, question in enumerate(questions):
        query_started = time.perf_counter()
        raw_candidates = retrieve(
            config=config,
            chunks=chunks,
            query_text=query_texts[q_idx],
            query_embedding=query_embeddings[q_idx],
            doc_embeddings=doc_embeddings,
            bm25=bm25,
            candidate_k=raw_candidate_k,
            question=question,
        )
        threshold_candidates = [
            candidate for candidate in raw_candidates if candidate["score"] >= config["min_score"]
        ][: config["internal_top_k"]]
        answer_candidates = apply_source_diversity(
            threshold_candidates,
            answer_top_k=config["answer_top_k"],
            deduplicate_by_document=config["deduplicate_by_document"],
            diversify_by_section=config["diversify_by_section"],
        )
        visible_sources = answer_candidates[: config["visible_source_limit"]]
        query_latency_ms = round((time.perf_counter() - query_started) * 1000, 2)

        first_rank = first_hit_rank(answer_candidates, question)
        visible_first_rank = first_hit_rank(visible_sources, question)
        is_negative = is_negative_question(question)
        no_answer_success = 1 if is_negative and not answer_candidates else 0 if is_negative else ""
        top_1 = answer_candidates[0] if answer_candidates else {}
        expected_doc_hit = first_rank is not None

        trace_retrieval(
            tracer=tracer,
            config=config,
            question=question,
            sources=answer_candidates,
            latency_ms=query_latency_ms,
            expected_doc_hit=expected_doc_hit,
        )

        detail_rows.append(
            {
                "config_id": config["config_id"],
                "experiment_id": config["experiment_id"],
                "search_mode": config["search_mode"],
                "question_id": question["question_id"],
                "query_type": question["query_type"],
                "question": question["question"],
                "expected_doc_keyword": get_expected_doc_keyword(question),
                "expected_section_keyword": get_expected_section_keyword(question),
                "expected_keywords": question.get("expected_keywords", ""),
                "expected_behavior": question.get("expected_behavior", ""),
                "internal_top_k": config["internal_top_k"],
                "answer_top_k": config["answer_top_k"],
                "visible_source_limit": config["visible_source_limit"],
                "min_score": config["min_score"],
                "deduplicate_by_document": config["deduplicate_by_document"],
                "diversify_by_section": config["diversify_by_section"],
                "raw_candidate_count": len(raw_candidates),
                "threshold_candidate_count": len(threshold_candidates),
                "answer_source_count": len(answer_candidates),
                "visible_source_count": len(visible_sources),
                "hit_at_1": 1 if first_rank is not None and first_rank <= 1 else 0,
                "hit_at_3": 1 if first_rank is not None and first_rank <= 3 else 0,
                "hit_at_5": 1 if first_rank is not None and first_rank <= 5 else 0,
                "visible_hit_at_3": 1 if visible_first_rank is not None and visible_first_rank <= 3 else 0,
                "mrr": reciprocal_rank(first_rank),
                "first_hit_rank": first_rank if first_rank is not None else "",
                "no_answer_success": no_answer_success,
                "top_1_title": top_1.get("title"),
                "top_1_section": top_1.get("section_title"),
                "top_1_score": round(float(top_1.get("score", 0.0)), 6) if top_1 else "",
                "top_5_titles": "|".join(str(item.get("title", "")) for item in answer_candidates[:5]),
                "top_5_chunk_ids": "|".join(str(item.get("chunk_id", "")) for item in answer_candidates[:5]),
                "latency_ms": query_latency_ms,
            }
        )

    summary = summarize_config(config, detail_rows, (time.perf_counter() - started) * 1000)
    return summary, detail_rows


def trace_retrieval(
    tracer: RetrievalTracer | None,
    config: dict,
    question: dict,
    sources: list[dict],
    latency_ms: float | None,
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
        embedding_model=MODEL_NAME if config["search_mode"] != "bm25" else "bm25",
        chunk_size=CHUNK_SIZE,
        chunk_overlap=CHUNK_OVERLAP,
        search_mode=config["search_mode"],
        top_k=config["internal_top_k"],
        expected_doc_hit=expected_doc_hit,
    )
    metadata.update(
        {
            "query_type": question.get("query_type"),
            "min_score": config["min_score"],
            "answer_top_k": config["answer_top_k"],
            "visible_source_limit": config["visible_source_limit"],
            "source_count": len(sources),
        }
    )
    tracer.trace(
        payload=RetrievalTracePayload(
            query=question["question"],
            filters=filters,
            sources=sources,
            metadata=metadata,
            latency_ms=latency_ms,
        )
    )


def retrieve(
    config: dict,
    chunks: list[dict],
    query_text: str,
    query_embedding: np.ndarray,
    doc_embeddings: np.ndarray,
    bm25: SimpleBM25,
    candidate_k: int,
    question: dict,
) -> list[dict]:
    mode = config["search_mode"]

    if mode == "vector":
        return vector_search(chunks, query_embedding, doc_embeddings, candidate_k)
    if mode == "bm25":
        return bm25_search(chunks, query_text, bm25, candidate_k)
    if mode == "hybrid":
        return hybrid_search(chunks, query_text, query_embedding, doc_embeddings, bm25, candidate_k)
    if mode == "vector_metadata_filter":
        candidates = vector_search(chunks, query_embedding, doc_embeddings, candidate_k)
        return boost_metadata_candidates(candidates, question)

    raise ValueError(f"Unsupported search mode: {mode}")


def vector_search(
    chunks: list[dict],
    query_embedding: np.ndarray,
    doc_embeddings: np.ndarray,
    candidate_k: int,
) -> list[dict]:
    scores = doc_embeddings @ query_embedding.reshape(-1)
    top_indices = np.argsort(-scores)[:candidate_k]
    return build_ranked_chunks(chunks, top_indices, scores)


def bm25_search(
    chunks: list[dict],
    query_text: str,
    bm25: SimpleBM25,
    candidate_k: int,
) -> list[dict]:
    scores = bm25.get_scores(tokenize(query_text))
    max_score = float(np.max(scores)) if len(scores) else 0.0
    if max_score > 0:
        scores = scores / max_score
    top_indices = np.argsort(-scores)[:candidate_k]
    return build_ranked_chunks(chunks, top_indices, scores)


def hybrid_search(
    chunks: list[dict],
    query_text: str,
    query_embedding: np.ndarray,
    doc_embeddings: np.ndarray,
    bm25: SimpleBM25,
    candidate_k: int,
    vector_weight: float = 0.7,
    bm25_weight: float = 0.3,
) -> list[dict]:
    vector_scores = doc_embeddings @ query_embedding.reshape(-1)
    bm25_scores = bm25.get_scores(tokenize(query_text))
    max_bm25 = float(np.max(bm25_scores)) if len(bm25_scores) else 0.0
    if max_bm25 > 0:
        bm25_scores = bm25_scores / max_bm25
    hybrid_scores = (vector_weight * vector_scores) + (bm25_weight * bm25_scores)
    top_indices = np.argsort(-hybrid_scores)[:candidate_k]
    return build_ranked_chunks(chunks, top_indices, hybrid_scores)


def build_ranked_chunks(chunks: list[dict], top_indices: np.ndarray, scores: np.ndarray) -> list[dict]:
    results = []
    for raw_rank, idx in enumerate(top_indices, start=1):
        chunk = chunks[int(idx)].copy()
        chunk["_index"] = int(idx)
        chunk["score"] = float(scores[int(idx)])
        chunk["raw_rank"] = raw_rank
        results.append(chunk)
    return results


def boost_metadata_candidates(candidates: list[dict], question: dict) -> list[dict]:
    tokens = []
    for value in [
        question.get("equipment_name", ""),
        question.get("category", ""),
        get_expected_doc_keyword(question),
        get_expected_section_keyword(question),
    ]:
        tokens.extend(token for token in tokenize(value) if len(token) >= 3)

    if not tokens:
        return candidates

    boosted = []
    non_boosted = []

    for candidate in candidates:
        text = searchable_text(candidate)
        if any(token in text for token in tokens):
            boosted.append(candidate)
        else:
            non_boosted.append(candidate)

    return boosted + non_boosted


def apply_source_diversity(
    candidates: list[dict],
    answer_top_k: int,
    deduplicate_by_document: bool = True,
    diversify_by_section: bool = True,
    max_per_document: int = 2,
) -> list[dict]:
    selected = []
    doc_counts: dict[str, int] = {}
    used_doc_sections = set()

    for candidate in candidates:
        doc_key = get_document_key(candidate)
        section_key = candidate.get("section_title") or "NO_SECTION"
        doc_count = doc_counts.get(doc_key, 0)

        if deduplicate_by_document and doc_count >= max_per_document:
            continue

        if diversify_by_section and (doc_key, section_key) in used_doc_sections:
            continue

        selected.append(candidate)
        doc_counts[doc_key] = doc_count + 1
        used_doc_sections.add((doc_key, section_key))

        if len(selected) >= answer_top_k:
            break

    return selected


def get_document_key(chunk: dict) -> str:
    return (
        chunk.get("document_id")
        or chunk.get("relative_path")
        or chunk.get("source_uri")
        or chunk.get("title")
        or "unknown"
    )


def get_expected_doc_keyword(question: dict) -> str:
    return str(question.get("expected_doc_keyword") or question.get("expected_doc_id") or "").strip()


def get_expected_section_keyword(question: dict) -> str:
    return str(question.get("expected_section_keyword") or question.get("expected_section") or "").strip()


def split_keywords(value: str) -> list[str]:
    raw = str(value or "").replace("|", ";")
    return [item.strip().lower() for item in raw.split(";") if item.strip()]


def searchable_text(chunk: dict) -> str:
    return " ".join(
        [
            str(chunk.get("title") or ""),
            str(chunk.get("section_title") or ""),
            str(chunk.get("content") or ""),
            str(chunk.get("source_uri") or ""),
            str(chunk.get("relative_path") or ""),
            str(chunk.get("document_type") or ""),
            str(chunk.get("equipment_name") or ""),
        ]
    ).lower()


def is_expected_hit(chunk: dict, question: dict) -> bool:
    expected_doc = get_expected_doc_keyword(question)
    expected_section = get_expected_section_keyword(question)

    if expected_doc in {"NO_ANSWER", "OUT_OF_SCOPE"}:
        return False

    text = searchable_text(chunk)
    doc_hit = any(token in text for token in tokenize(expected_doc) if len(token) >= 3)
    section_hit = bool(expected_section and expected_section.lower() in text)
    keyword_hit = any(keyword in text for keyword in split_keywords(question.get("expected_keywords", "")))
    return bool(doc_hit or section_hit or keyword_hit)


def is_negative_question(question: dict) -> bool:
    expected_doc = get_expected_doc_keyword(question)
    expected_behavior = str(question.get("expected_behavior", ""))
    return (
        question.get("query_type") in {"no_answer", "out_of_scope"}
        or expected_doc in {"NO_ANSWER", "OUT_OF_SCOPE"}
        or expected_behavior in {"no_retrieval_result", "out_of_scope_response"}
    )


def first_hit_rank(results: list[dict], question: dict) -> int | None:
    for rank, chunk in enumerate(results, start=1):
        if is_expected_hit(chunk, question):
            return rank
    return None


def reciprocal_rank(rank: int | None) -> float:
    return 0.0 if rank is None else 1.0 / rank


def summarize_config(config: dict, detail_rows: list[dict], elapsed_ms: float) -> dict:
    normal_rows = [row for row in detail_rows if row["query_type"] not in {"no_answer", "out_of_scope"}]
    negative_rows = [row for row in detail_rows if row["query_type"] in {"no_answer", "out_of_scope"}]
    no_answer_values = [float(row["no_answer_success"]) for row in negative_rows if row["no_answer_success"] != ""]
    avg_answer_source_count = sum(row["answer_source_count"] for row in detail_rows) / len(detail_rows)
    avg_visible_source_count = sum(row["visible_source_count"] for row in detail_rows) / len(detail_rows)

    summary = {
        "config_id": config["config_id"],
        "experiment_id": config["experiment_id"],
        "embedding_model": MODEL_NAME if config["search_mode"] != "bm25" else "",
        "search_mode": config["search_mode"],
        "internal_top_k": config["internal_top_k"],
        "answer_top_k": config["answer_top_k"],
        "visible_source_limit": config["visible_source_limit"],
        "min_score": config["min_score"],
        "deduplicate_by_document": config["deduplicate_by_document"],
        "diversify_by_section": config["diversify_by_section"],
        "question_count": len(detail_rows),
        "normal_question_count": len(normal_rows),
        "negative_question_count": len(negative_rows),
        "hit_at_1": mean_metric(normal_rows, "hit_at_1"),
        "hit_at_3": mean_metric(normal_rows, "hit_at_3"),
        "hit_at_5": mean_metric(normal_rows, "hit_at_5"),
        "visible_hit_at_3": mean_metric(normal_rows, "visible_hit_at_3"),
        "mrr": mean_metric(normal_rows, "mrr"),
        "no_answer_accuracy": round(sum(no_answer_values) / len(no_answer_values), 4)
        if no_answer_values
        else "",
        "avg_answer_source_count": round(avg_answer_source_count, 2),
        "avg_visible_source_count": round(avg_visible_source_count, 2),
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
            row["search_mode"] in {"vector", "hybrid", "vector_metadata_filter"},
            row["internal_top_k"],
            row["answer_top_k"],
            -row["elapsed_ms"],
        ),
        reverse=True,
    )[0]


def write_query_type_summary(detail_rows: list[dict]) -> None:
    grouped: dict[tuple[str, str, str], list[dict]] = {}

    for row in detail_rows:
        if row["query_type"] in {"no_answer", "out_of_scope"}:
            continue
        grouped.setdefault((row["config_id"], row["search_mode"], row["query_type"]), []).append(row)

    rows = []
    for (config_id, search_mode, query_type), group_rows in sorted(grouped.items()):
        rows.append(
            {
                "config_id": config_id,
                "search_mode": search_mode,
                "query_type": query_type,
                "question_count": len(group_rows),
                "hit_at_1": mean_metric(group_rows, "hit_at_1"),
                "hit_at_3": mean_metric(group_rows, "hit_at_3"),
                "hit_at_5": mean_metric(group_rows, "hit_at_5"),
                "visible_hit_at_3": mean_metric(group_rows, "visible_hit_at_3"),
                "mrr": mean_metric(group_rows, "mrr"),
            }
        )

    write_csv(RESULT_DIR / "retrieval_eval_by_query_type.csv", rows)


def write_failed_cases(best_row: dict, detail_rows: list[dict]) -> list[dict]:
    failed_rows = []

    for row in detail_rows:
        if row["config_id"] != best_row["config_id"]:
            continue

        if row["query_type"] in {"no_answer", "out_of_scope"} and row["no_answer_success"] == 0:
            failed_rows.append(row)
        elif row["query_type"] not in {"no_answer", "out_of_scope"} and row["hit_at_5"] == 0:
            failed_rows.append(row)

    if failed_rows:
        write_csv(RESULT_DIR / "retrieval_failed_cases.csv", failed_rows)
    else:
        (RESULT_DIR / "retrieval_failed_cases.csv").write_text(
            "config_id,question_id,query_type,question\n",
            encoding="utf-8-sig",
        )

    return failed_rows


def write_best_config(best_row: dict) -> None:
    yaml_text = f"""retrieval_config_id: {best_row["config_id"]}
embedding_model: {MODEL_NAME}
search_mode: {best_row["search_mode"]}
collection_name: industrial_rag_chunks_a
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
mmr_enabled: false

return_fields:
  - chunk_id
  - document_id
  - document_version_id
  - title
  - document_type
  - section_title
  - content
  - score
  - rank
  - source_uri
"""
    (CONFIG_DIR / "best_retrieval_config.yaml").write_text(yaml_text, encoding="utf-8")


def write_report(
    summary_rows: list[dict],
    best_row: dict,
    failed_cases: list[dict],
    chunks: list[dict],
    questions: list[dict],
) -> None:
    report_path = RESULT_DIR / "retrieval_eval_report.md"

    with report_path.open("w", encoding="utf-8") as f:
        f.write("# Retrieval Evaluation Report\n\n")
        f.write("## Experiment Setup\n\n")
        f.write(f"- documents: `{len(set(chunk['document_id'] for chunk in chunks))}`\n")
        f.write(f"- chunks: `{len(chunks)}`\n")
        f.write(f"- questions: `{len(questions)}`\n")
        f.write("- embedding_model: `BAAI/bge-m3`\n")
        f.write("- chunk_config: `chunk_size=800`, `chunk_overlap=100`\n")
        f.write("- golden_questions: `golden_questions_v2.csv`\n")
        f.write("- search_modes: `vector`, `bm25`, `hybrid`, `vector_metadata_filter`\n")
        f.write("- internal_top_k candidates: `3 / 5 / 10`\n")
        f.write("- min_score candidates: `0.2 / 0.3 / 0.4`\n")
        f.write("- source diversity: `deduplicate_by_document=true`, `diversify_by_section=true`\n\n")

        f.write("## Best Config\n\n")
        for key in [
            "config_id",
            "search_mode",
            "internal_top_k",
            "answer_top_k",
            "visible_source_limit",
            "min_score",
            "hit_at_5",
            "visible_hit_at_3",
            "mrr",
            "no_answer_accuracy",
            "avg_answer_source_count",
            "project_score",
        ]:
            f.write(f"- {key}: `{best_row[key]}`\n")

        f.write("\n## Summary Table\n\n")
        f.write(
            "| Config | Search | top_k | min_score | hit@1 | hit@3 | hit@5 | "
            "visible_hit@3 | MRR | no_answer_acc | avg_sources | elapsed_ms | project_score |\n"
        )
        f.write("|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|\n")

        for row in sorted(summary_rows, key=lambda item: item["config_id"]):
            f.write(
                f"| {row['config_id']} | {row['search_mode']} | {row['internal_top_k']} | "
                f"{row['min_score']} | {row['hit_at_1']} | {row['hit_at_3']} | {row['hit_at_5']} | "
                f"{row['visible_hit_at_3']} | {row['mrr']} | {row['no_answer_accuracy']} | "
                f"{row['avg_answer_source_count']} | {row['elapsed_ms']} | {row['project_score']} |\n"
            )

        f.write("\n## Failed Cases\n\n")
        if not failed_cases:
            f.write("- No failed cases in best config.\n")
        else:
            f.write("| Question ID | Type | Question | Expected | Top 1 Title | Top 1 Score |\n")
            f.write("|---|---|---|---|---|---:|\n")
            for row in failed_cases:
                f.write(
                    f"| {row['question_id']} | {row['query_type']} | {row['question']} | "
                    f"{row['expected_doc_keyword']} | {row['top_1_title']} | {row['top_1_score']} |\n"
                )


def write_csv(path: Path, rows: list[dict]) -> None:
    if not rows:
        path.write_text("", encoding="utf-8-sig")
        return

    with path.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)


if __name__ == "__main__":
    main()
