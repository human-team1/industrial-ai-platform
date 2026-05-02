from __future__ import annotations

import csv
import json
import os
import re
import sys
import time
from pathlib import Path

import numpy as np
from dotenv import load_dotenv

sys.path.append(str(Path(__file__).resolve().parents[2]))

from rag.scripts.run_retrieval_experiment import (
    CACHE_DIR as RETRIEVAL_CACHE_DIR,
    CHUNK_OVERLAP,
    CHUNK_SIZE,
    CHUNK_RECORDS_PATH,
    MODEL_NAME,
    QUESTIONS_PATH,
    build_chunk_text,
    build_query_text,
    load_jsonl,
    load_or_create_embeddings,
    normalize,
)


PROJECT_RESULT_DIR = Path("experiments/rag_langgraph_eval/results")
RESULT_DIR = PROJECT_RESULT_DIR / "reranker_eval"
RERANKER_MODEL = os.getenv("RAG_RERANKER_MODEL", "BAAI/bge-reranker-v2-m3")


def main() -> None:
    load_dotenv()
    RESULT_DIR.mkdir(parents=True, exist_ok=True)

    started = time.perf_counter()
    chunks = load_jsonl(CHUNK_RECORDS_PATH)
    questions = load_questions(QUESTIONS_PATH)
    chunk_texts = [build_chunk_text(chunk) for chunk in chunks]
    query_texts = [build_query_text(row) for row in questions]

    try:
        doc_embeddings, query_embeddings = load_or_create_embeddings(chunks, chunk_texts, query_texts)
        reranker = create_reranker()
        detail_rows = evaluate_reranker(
            reranker=reranker,
            chunks=chunks,
            questions=questions,
            query_texts=query_texts,
            doc_embeddings=doc_embeddings,
            query_embeddings=query_embeddings,
        )
        summary = summarize(detail_rows, elapsed_ms=(time.perf_counter() - started) * 1000)
        summary["status"] = "completed"
        summary["skip_reason"] = ""
        write_csv(RESULT_DIR / "reranker_eval_detail.csv", detail_rows)
        write_csv(RESULT_DIR / "reranker_eval_summary.csv", [summary])
        write_report(summary, detail_rows)
    except Exception as exc:
        summary = {
            "config_id": "R5_HYBRID_RERANKER",
            "status": "skipped",
            "skip_reason": f"{exc.__class__.__name__}: {exc}",
            "embedding_model": MODEL_NAME,
            "reranker_model": RERANKER_MODEL,
            "question_count": len(questions),
            "hit_at_1": "",
            "hit_at_3": "",
            "hit_at_5": "",
            "visible_hit_at_3": "",
            "mrr": "",
            "no_answer_accuracy": "",
            "elapsed_ms": round((time.perf_counter() - started) * 1000, 2),
        }
        write_csv(RESULT_DIR / "reranker_eval_summary.csv", [summary])
        write_report(summary, [])
        print(f"reranker_status: skipped")
        print(f"reason: {summary['skip_reason']}")
        print(f"created: {RESULT_DIR / 'reranker_eval_summary.csv'}")
        print(f"created: {RESULT_DIR / 'reranker_eval_report.md'}")
        return

    print("reranker_status: completed")
    print(f"created: {RESULT_DIR / 'reranker_eval_summary.csv'}")
    print(f"created: {RESULT_DIR / 'reranker_eval_detail.csv'}")
    print(f"created: {RESULT_DIR / 'reranker_eval_report.md'}")


def load_questions(path: Path) -> list[dict]:
    with path.open("r", encoding="utf-8-sig", newline="") as f:
        return list(csv.DictReader(f))


def create_reranker():
    try:
        from sentence_transformers import CrossEncoder
    except ImportError as exc:
        raise RuntimeError("sentence-transformers CrossEncoder is not installed") from exc

    cache_folder = Path(os.getenv("HF_HOME", RETRIEVAL_CACHE_DIR / "huggingface")).resolve()
    os.environ.setdefault("HF_HOME", str(cache_folder))
    os.environ.setdefault("HUGGINGFACE_HUB_CACHE", str(cache_folder / "hub"))
    os.environ.setdefault("TRANSFORMERS_CACHE", str(cache_folder / "transformers"))
    return CrossEncoder(
        RERANKER_MODEL,
        max_length=512,
    )


def evaluate_reranker(
    reranker,
    chunks: list[dict],
    questions: list[dict],
    query_texts: list[str],
    doc_embeddings: np.ndarray,
    query_embeddings: np.ndarray,
) -> list[dict]:
    detail_rows = []

    for q_idx, question in enumerate(questions):
        query_started = time.perf_counter()
        raw_candidates = vector_candidates(
            chunks=chunks,
            query_embedding=query_embeddings[q_idx],
            doc_embeddings=doc_embeddings,
            candidate_k=20,
        )
        reranked = rerank(
            reranker=reranker,
            query=query_texts[q_idx],
            candidates=raw_candidates,
        )
        threshold_candidates = [candidate for candidate in reranked if candidate["rerank_score"] >= 0.0][:10]
        answer_candidates = apply_diversity(threshold_candidates, answer_top_k=5)
        visible_sources = answer_candidates[:3]

        first_rank = first_hit_rank(answer_candidates, question)
        visible_first_rank = first_hit_rank(visible_sources, question)
        is_negative = is_negative_question(question)
        no_answer_success = 1 if is_negative and not answer_candidates else 0 if is_negative else ""
        top_1 = answer_candidates[0] if answer_candidates else {}

        detail_rows.append(
            {
                "config_id": "R5_HYBRID_RERANKER",
                "embedding_model": MODEL_NAME,
                "reranker_model": RERANKER_MODEL,
                "question_id": question["question_id"],
                "query_type": question["query_type"],
                "question": question["question"],
                "expected_doc_keyword": expected_doc(question),
                "expected_section_keyword": expected_section(question),
                "raw_candidate_count": len(raw_candidates),
                "answer_source_count": len(answer_candidates),
                "visible_source_count": len(visible_sources),
                "hit_at_1": int(first_rank is not None and first_rank <= 1),
                "hit_at_3": int(first_rank is not None and first_rank <= 3),
                "hit_at_5": int(first_rank is not None and first_rank <= 5),
                "visible_hit_at_3": int(visible_first_rank is not None and visible_first_rank <= 3),
                "mrr": 0.0 if first_rank is None else round(1.0 / first_rank, 4),
                "first_hit_rank": first_rank or "",
                "no_answer_success": no_answer_success,
                "top_1_title": top_1.get("title", ""),
                "top_1_section": top_1.get("section_title", ""),
                "top_1_score": round(float(top_1.get("score", 0.0)), 6) if top_1 else "",
                "top_1_rerank_score": round(float(top_1.get("rerank_score", 0.0)), 6) if top_1 else "",
                "latency_ms": round((time.perf_counter() - query_started) * 1000, 2),
            }
        )

    return detail_rows


def vector_candidates(
    chunks: list[dict],
    query_embedding: np.ndarray,
    doc_embeddings: np.ndarray,
    candidate_k: int,
) -> list[dict]:
    doc_embeddings = normalize(doc_embeddings)
    query_embedding = normalize(query_embedding.reshape(1, -1))[0]
    scores = doc_embeddings @ query_embedding.reshape(-1)
    top_indices = np.argsort(-scores)[:candidate_k]
    candidates = []
    for rank, idx in enumerate(top_indices, start=1):
        chunk = chunks[int(idx)].copy()
        chunk["_index"] = int(idx)
        chunk["raw_rank"] = rank
        chunk["score"] = float(scores[int(idx)])
        candidates.append(chunk)
    return candidates


def rerank(reranker, query: str, candidates: list[dict]) -> list[dict]:
    pairs = [(query, build_chunk_text(candidate)) for candidate in candidates]
    scores = reranker.predict(pairs, convert_to_numpy=True, show_progress_bar=False)
    reranked = []
    for candidate, score in zip(candidates, scores):
        row = candidate.copy()
        row["rerank_score"] = float(score)
        reranked.append(row)
    return sorted(reranked, key=lambda item: item["rerank_score"], reverse=True)


def apply_diversity(candidates: list[dict], answer_top_k: int) -> list[dict]:
    selected = []
    seen_docs = set()
    seen_sections = set()

    for candidate in candidates:
        doc_key = candidate.get("document_id")
        section_key = (candidate.get("document_id"), candidate.get("section_title"))
        if doc_key in seen_docs and section_key in seen_sections:
            continue
        selected.append({**candidate, "rank": len(selected) + 1})
        seen_docs.add(doc_key)
        seen_sections.add(section_key)
        if len(selected) >= answer_top_k:
            break

    return selected


def summarize(detail_rows: list[dict], elapsed_ms: float) -> dict:
    normal_rows = [row for row in detail_rows if row["query_type"] not in {"no_answer", "out_of_scope"}]
    negative_rows = [row for row in detail_rows if row["query_type"] in {"no_answer", "out_of_scope"}]
    no_answer_values = [int(row["no_answer_success"]) for row in negative_rows if row["no_answer_success"] != ""]
    return {
        "config_id": "R5_HYBRID_RERANKER",
        "embedding_model": MODEL_NAME,
        "reranker_model": RERANKER_MODEL,
        "question_count": len(detail_rows),
        "normal_question_count": len(normal_rows),
        "negative_question_count": len(negative_rows),
        "hit_at_1": mean(normal_rows, "hit_at_1"),
        "hit_at_3": mean(normal_rows, "hit_at_3"),
        "hit_at_5": mean(normal_rows, "hit_at_5"),
        "visible_hit_at_3": mean(normal_rows, "visible_hit_at_3"),
        "mrr": mean(normal_rows, "mrr"),
        "no_answer_accuracy": round(sum(no_answer_values) / len(no_answer_values), 4) if no_answer_values else "",
        "elapsed_ms": round(elapsed_ms, 2),
    }


def write_report(summary: dict, detail_rows: list[dict]) -> None:
    lines = [
        "# Cross-encoder Reranker Evaluation Report",
        "",
        "## Summary",
        "",
    ]
    for key, value in summary.items():
        lines.append(f"- {key}: `{value}`")

    if summary.get("status") == "skipped":
        lines.extend(
            [
                "",
                "## 해석",
                "",
                "cross-encoder reranker 실험은 코드상 구현되어 있으나, 현재 로컬에 reranker 모델 파일이 없어 실행되지 않았다.",
                "`RAG_RERANKER_MODEL`에 사용할 모델을 지정하고 Hugging Face cache에 모델을 준비하면 같은 스크립트로 실행할 수 있다.",
            ]
        )
    elif detail_rows:
        failed = [
            row
            for row in detail_rows
            if (row["query_type"] in {"no_answer", "out_of_scope"} and row["no_answer_success"] == 0)
            or (row["query_type"] not in {"no_answer", "out_of_scope"} and row["hit_at_5"] == 0)
        ]
        lines.extend(
            [
                "",
                "## Failed Cases",
                "",
                "| question_id | query_type | question | top_1_title | top_1_rerank_score |",
                "|---|---|---|---|---:|",
            ]
        )
        for row in failed:
            lines.append(
                f"| {row['question_id']} | {row['query_type']} | {row['question']} | "
                f"{row['top_1_title']} | {row['top_1_rerank_score']} |"
            )

    (RESULT_DIR / "reranker_eval_report.md").write_text("\n".join(lines) + "\n", encoding="utf-8")


def write_csv(path: Path, rows: list[dict]) -> None:
    if not rows:
        path.write_text("", encoding="utf-8-sig")
        return
    with path.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)


def mean(rows: list[dict], key: str) -> float:
    if not rows:
        return 0.0
    return round(sum(float(row[key]) for row in rows) / len(rows), 4)


def first_hit_rank(results: list[dict], question: dict) -> int | None:
    for rank, chunk in enumerate(results, start=1):
        if is_expected_hit(chunk, question):
            return rank
    return None


def is_expected_hit(chunk: dict, question: dict) -> bool:
    doc = expected_doc(question)
    section = expected_section(question)
    if doc in {"NO_ANSWER", "OUT_OF_SCOPE"}:
        return False
    text = searchable_text(chunk)
    doc_hit = any(token in text for token in tokenize(doc) if len(token) >= 3)
    section_hit = bool(section and section.lower() in text)
    keyword_hit = any(keyword in text for keyword in split_keywords(question.get("expected_keywords", "")))
    return bool(doc_hit or section_hit or keyword_hit)


def is_negative_question(question: dict) -> bool:
    doc = expected_doc(question)
    expected_behavior = str(question.get("expected_behavior", ""))
    return (
        question.get("query_type") in {"no_answer", "out_of_scope"}
        or doc in {"NO_ANSWER", "OUT_OF_SCOPE"}
        or expected_behavior in {"no_retrieval_result", "out_of_scope_response"}
    )


def expected_doc(question: dict) -> str:
    return str(question.get("expected_doc_keyword") or question.get("expected_doc_id") or "").strip()


def expected_section(question: dict) -> str:
    return str(question.get("expected_section_keyword") or question.get("expected_section") or "").strip()


def searchable_text(chunk: dict) -> str:
    return " ".join(
        str(chunk.get(key) or "")
        for key in ["document_id", "title", "section_title", "content", "source_uri"]
    ).lower()


def split_keywords(value: str) -> list[str]:
    return [keyword.strip().lower() for keyword in re.split(r"[;|,]", str(value)) if keyword.strip()]


def tokenize(text: str) -> list[str]:
    return re.findall(r"[a-zA-Z0-9가-힣_\-/]+", str(text).lower())


if __name__ == "__main__":
    main()
