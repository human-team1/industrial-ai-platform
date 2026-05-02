from __future__ import annotations

import csv
import hashlib
import json
import math
import os
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

from dotenv import load_dotenv


CHUNK_RECORDS_PATH = Path(
    "experiments/rag_langgraph_eval/results/chunking_matrix/C3_chunk_records.jsonl"
)
QUESTIONS_PATH = Path("experiments/rag_langgraph_eval/datasets/golden_questions_v1.csv")
RESULT_DIR = Path("experiments/rag_langgraph_eval/results/embedding_eval")
CACHE_DIR = RESULT_DIR / "cache"


MODEL_CANDIDATES = [
    {
        "experiment_id": "E_OPENAI_SMALL",
        "provider": "openai",
        "model": "text-embedding-3-small",
        "purpose": "OpenAI embedding baseline",
    },
    {
        "experiment_id": "E_GEMINI_EMBEDDING",
        "provider": "gemini",
        "model": "gemini-embedding-001",
        "purpose": "Gemini embedding quota fallback",
    },
    {
        "experiment_id": "E_BGE_M3",
        "provider": "sentence_transformers",
        "model": "BAAI/bge-m3",
        "purpose": "한국어/영문/혼합 질의 local 후보",
    },
    {
        "experiment_id": "E_E5_MULTI",
        "provider": "sentence_transformers",
        "model": "intfloat/multilingual-e5-large",
        "purpose": "multilingual e5 local 후보",
    },
    {
        "experiment_id": "E_KOSIMCSE",
        "provider": "sentence_transformers",
        "model": "BM-K/KoSimCSE-roberta-multitask",
        "purpose": "한국어 질의 local 후보",
    },
]


@dataclass
class GoldenQuestion:
    question_id: str
    category: str
    query: str
    expected_terms: list[str]
    notes: str


def main() -> None:
    load_dotenv()
    RESULT_DIR.mkdir(parents=True, exist_ok=True)
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    configure_model_cache()

    chunks = load_chunks(CHUNK_RECORDS_PATH)
    questions = load_questions(QUESTIONS_PATH)

    summary_rows = []
    detail_rows = []

    for candidate in MODEL_CANDIDATES:
        started = time.perf_counter()
        status = "completed"
        skip_reason = ""

        try:
            embedder = create_embedder(candidate)
            chunk_vectors = get_or_create_chunk_vectors(embedder, candidate, chunks)
            query_vectors = embedder.embed_texts([question.query for question in questions])
            model_detail_rows = evaluate_model(
                candidate,
                chunks,
                questions,
                chunk_vectors,
                query_vectors,
            )
            detail_rows.extend(model_detail_rows)
            summary_rows.append(
                build_summary_row(
                    candidate,
                    model_detail_rows,
                    elapsed_ms=(time.perf_counter() - started) * 1000,
                    status=status,
                    skip_reason=skip_reason,
                )
            )
        except SkipExperiment as exc:
            status = "skipped"
            skip_reason = str(exc)
            summary_rows.append(
                build_skipped_summary_row(
                    candidate,
                    elapsed_ms=(time.perf_counter() - started) * 1000,
                    skip_reason=skip_reason,
                )
            )
        except Exception as exc:
            status = "failed"
            skip_reason = f"{exc.__class__.__name__}: {exc}"
            summary_rows.append(
                build_failed_summary_row(
                    candidate,
                    elapsed_ms=(time.perf_counter() - started) * 1000,
                    failure_reason=skip_reason,
                )
            )

    write_csv(RESULT_DIR / "embedding_eval_detail.csv", detail_rows)
    write_csv(RESULT_DIR / "embedding_eval_summary.csv", summary_rows)
    write_report(summary_rows)

    print(f"chunks: {len(chunks)}")
    print(f"questions: {len(questions)}")
    print(f"created: {RESULT_DIR / 'embedding_eval_summary.csv'}")
    print(f"created: {RESULT_DIR / 'embedding_eval_detail.csv'}")
    print(f"created: {RESULT_DIR / 'embedding_eval_report.md'}")


def configure_model_cache() -> None:
    model_cache_dir = (CACHE_DIR / "huggingface").resolve()
    model_cache_dir.mkdir(parents=True, exist_ok=True)
    os.environ.setdefault("HF_HOME", str(model_cache_dir))
    os.environ.setdefault("HUGGINGFACE_HUB_CACHE", str(model_cache_dir / "hub"))
    os.environ.setdefault("TRANSFORMERS_CACHE", str(model_cache_dir / "transformers"))
    os.environ.setdefault("HF_HUB_DISABLE_SYMLINKS_WARNING", "1")


def load_chunks(path: Path) -> list[dict]:
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines()]


def load_questions(path: Path) -> list[GoldenQuestion]:
    rows = []

    with path.open("r", encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)

        for row in reader:
            rows.append(
                GoldenQuestion(
                    question_id=row["question_id"],
                    category=row["category"],
                    query=row["query"],
                    expected_terms=[
                        term.strip()
                        for term in row["expected_terms"].split("|")
                        if term.strip()
                    ],
                    notes=row["notes"],
                )
            )

    return rows


class SkipExperiment(RuntimeError):
    pass


class OpenAIEmbedder:
    def __init__(self, model: str) -> None:
        api_key = os.getenv("OPENAI_API_KEY", "").strip()

        if not api_key:
            raise SkipExperiment("OPENAI_API_KEY is empty")

        try:
            from openai import OpenAI
        except ImportError as exc:
            raise SkipExperiment("openai package is not installed") from exc

        self.client = OpenAI(api_key=api_key)
        self.model = model

    def embed_texts(self, texts: list[str]) -> list[list[float]]:
        vectors: list[list[float]] = []
        batch_size = 64

        for batch in batched(texts, batch_size):
            response = self.client.embeddings.create(model=self.model, input=batch)
            vectors.extend([item.embedding for item in response.data])

        return vectors


class SentenceTransformersEmbedder:
    def __init__(self, model: str) -> None:
        try:
            from sentence_transformers import SentenceTransformer
        except ImportError as exc:
            raise SkipExperiment("sentence-transformers package is not installed") from exc

        self.model_name = model
        self.model = SentenceTransformer(model)

    def embed_texts(self, texts: list[str]) -> list[list[float]]:
        vectors = self.model.encode(
            texts,
            batch_size=32,
            normalize_embeddings=True,
            show_progress_bar=False,
        )
        return vectors.tolist()


class GeminiEmbedder:
    def __init__(self, model: str) -> None:
        api_key = (
            os.getenv("GEMINI_API_KEY", "").strip()
            or os.getenv("GOOGLE_API_KEY", "").strip()
            or os.getenv("GOOGLE_GENAI_API_KEY", "").strip()
        )

        if not api_key:
            raise SkipExperiment("GEMINI_API_KEY or GOOGLE_API_KEY is empty")

        try:
            import requests
        except ImportError as exc:
            raise SkipExperiment("requests package is not installed") from exc

        self.requests = requests
        self.model = model
        self.endpoint = (
            "https://generativelanguage.googleapis.com/v1beta/"
            f"models/{model}:embedContent"
        )
        self.headers = {
            "x-goog-api-key": api_key,
            "Content-Type": "application/json",
        }

    def embed_texts(self, texts: list[str]) -> list[list[float]]:
        vectors = []

        for text in texts:
            payload = {
                "model": f"models/{self.model}",
                "content": {"parts": [{"text": text}]},
                "task_type": "SEMANTIC_SIMILARITY",
                "output_dimensionality": 768,
            }
            response = self.requests.post(
                self.endpoint,
                headers=self.headers,
                json=payload,
                timeout=60,
            )
            response.raise_for_status()
            body = response.json()
            vectors.append(body["embedding"]["values"])

        return vectors


def create_embedder(candidate: dict):
    if candidate["provider"] == "openai":
        return OpenAIEmbedder(candidate["model"])

    if candidate["provider"] == "gemini":
        return GeminiEmbedder(candidate["model"])

    if candidate["provider"] == "sentence_transformers":
        return SentenceTransformersEmbedder(candidate["model"])

    raise SkipExperiment(f"Unsupported provider: {candidate['provider']}")


def get_or_create_chunk_vectors(embedder, candidate: dict, chunks: list[dict]) -> list[list[float]]:
    cache_path = CACHE_DIR / f"{candidate['experiment_id']}_{records_hash(chunks)}.json"

    if cache_path.exists():
        return json.loads(cache_path.read_text(encoding="utf-8"))

    texts = [build_retrieval_text(chunk) for chunk in chunks]
    vectors = embedder.embed_texts(texts)
    cache_path.write_text(json.dumps(vectors), encoding="utf-8")
    return vectors


def evaluate_model(
    candidate: dict,
    chunks: list[dict],
    questions: list[GoldenQuestion],
    chunk_vectors: list[list[float]],
    query_vectors: list[list[float]],
) -> list[dict]:
    rows = []

    for question, query_vector in zip(questions, query_vectors):
        ranked = rank_chunks(query_vector, chunk_vectors, chunks)
        top_5 = ranked[:5]
        first_hit_rank = find_first_hit_rank(question.expected_terms, ranked)

        rows.append(
            {
                "experiment_id": candidate["experiment_id"],
                "provider": candidate["provider"],
                "model": candidate["model"],
                "question_id": question.question_id,
                "category": question.category,
                "query": question.query,
                "expected_terms": "|".join(question.expected_terms),
                "hit_at_1": int(first_hit_rank == 1),
                "hit_at_3": int(first_hit_rank is not None and first_hit_rank <= 3),
                "hit_at_5": int(first_hit_rank is not None and first_hit_rank <= 5),
                "mrr": round(1 / first_hit_rank, 4) if first_hit_rank else 0,
                "first_hit_rank": first_hit_rank or "",
                "top_1_chunk_id": top_5[0]["chunk"]["chunk_id"],
                "top_1_title": top_5[0]["chunk"]["title"],
                "top_1_score": round(top_5[0]["score"], 6),
                "top_5_chunk_ids": "|".join(item["chunk"]["chunk_id"] for item in top_5),
            }
        )

    return rows


def rank_chunks(
    query_vector: list[float],
    chunk_vectors: list[list[float]],
    chunks: list[dict],
) -> list[dict]:
    scored = [
        {
            "chunk": chunk,
            "score": cosine_similarity(query_vector, chunk_vector),
        }
        for chunk, chunk_vector in zip(chunks, chunk_vectors)
    ]
    return sorted(scored, key=lambda item: item["score"], reverse=True)


def find_first_hit_rank(expected_terms: list[str], ranked: list[dict]) -> int | None:
    for index, item in enumerate(ranked, start=1):
        haystack = build_match_text(item["chunk"]).lower()

        if any(term.lower() in haystack for term in expected_terms):
            return index

    return None


def build_summary_row(
    candidate: dict,
    detail_rows: list[dict],
    elapsed_ms: float,
    status: str,
    skip_reason: str,
) -> dict:
    count = len(detail_rows)

    return {
        "experiment_id": candidate["experiment_id"],
        "provider": candidate["provider"],
        "model": candidate["model"],
        "status": status,
        "skip_reason": skip_reason,
        "question_count": count,
        "hit_at_1": round(sum(row["hit_at_1"] for row in detail_rows) / count, 4),
        "hit_at_3": round(sum(row["hit_at_3"] for row in detail_rows) / count, 4),
        "hit_at_5": round(sum(row["hit_at_5"] for row in detail_rows) / count, 4),
        "mrr": round(sum(row["mrr"] for row in detail_rows) / count, 4),
        "elapsed_ms": round(elapsed_ms, 2),
    }


def build_skipped_summary_row(
    candidate: dict,
    elapsed_ms: float,
    skip_reason: str,
) -> dict:
    return {
        "experiment_id": candidate["experiment_id"],
        "provider": candidate["provider"],
        "model": candidate["model"],
        "status": "skipped",
        "skip_reason": skip_reason,
        "question_count": 0,
        "hit_at_1": "",
        "hit_at_3": "",
        "hit_at_5": "",
        "mrr": "",
        "elapsed_ms": round(elapsed_ms, 2),
    }


def build_failed_summary_row(
    candidate: dict,
    elapsed_ms: float,
    failure_reason: str,
) -> dict:
    return {
        "experiment_id": candidate["experiment_id"],
        "provider": candidate["provider"],
        "model": candidate["model"],
        "status": "failed",
        "skip_reason": failure_reason,
        "question_count": 0,
        "hit_at_1": "",
        "hit_at_3": "",
        "hit_at_5": "",
        "mrr": "",
        "elapsed_ms": round(elapsed_ms, 2),
    }


def write_csv(path: Path, rows: list[dict]) -> None:
    if not rows:
        return

    with path.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)


def write_report(rows: list[dict]) -> None:
    lines = [
        "# Embedding Evaluation Report",
        "",
        "기준 chunk: C3 (`chunk_size=800`, `overlap=100`)",
        "",
        "| ID | Provider | Model | Status | hit@1 | hit@3 | hit@5 | MRR | Elapsed ms | Skip Reason |",
        "| --- | --- | --- | --- | ---: | ---: | ---: | ---: | ---: | --- |",
    ]

    for row in rows:
        lines.append(
            "| {experiment_id} | {provider} | {model} | {status} | {hit_at_1} | "
            "{hit_at_3} | {hit_at_5} | {mrr} | {elapsed_ms} | {skip_reason} |".format(
                **row
            )
        )

    lines.extend(
        [
            "",
            "## Notes",
            "",
            "- OpenAI 실험은 `OPENAI_API_KEY`와 `openai==1.54.4`가 필요하다.",
            "- Local 실험은 `sentence-transformers`, `torch`, `transformers`와 모델 다운로드가 필요하다.",
            "- hit 판정은 golden question의 `expected_terms`가 검색 결과 chunk의 title/section/content/source_uri 중 하나에 포함되는지로 계산한다.",
            "",
        ]
    )

    (RESULT_DIR / "embedding_eval_report.md").write_text(
        "\n".join(lines),
        encoding="utf-8",
    )


def build_retrieval_text(chunk: dict) -> str:
    return "\n".join(
        value
        for value in [
            chunk.get("title", ""),
            chunk.get("section_title") or "",
            chunk.get("document_type", ""),
            chunk.get("content", ""),
        ]
        if value
    )


def build_match_text(chunk: dict) -> str:
    return "\n".join(
        str(value)
        for value in [
            chunk.get("title", ""),
            chunk.get("section_title") or "",
            chunk.get("content", ""),
            chunk.get("source_uri", ""),
            chunk.get("relative_path", ""),
            chunk.get("document_type", ""),
        ]
        if value
    )


def cosine_similarity(left: list[float], right: list[float]) -> float:
    dot = sum(a * b for a, b in zip(left, right))
    left_norm = math.sqrt(sum(a * a for a in left))
    right_norm = math.sqrt(sum(b * b for b in right))

    if left_norm == 0 or right_norm == 0:
        return 0

    return dot / (left_norm * right_norm)


def batched(items: list[str], batch_size: int) -> Iterable[list[str]]:
    for index in range(0, len(items), batch_size):
        yield items[index : index + batch_size]


def records_hash(chunks: list[dict]) -> str:
    payload = "|".join(chunk["chunk_id"] for chunk in chunks)
    return hashlib.sha1(payload.encode("utf-8")).hexdigest()[:12]


if __name__ == "__main__":
    main()
