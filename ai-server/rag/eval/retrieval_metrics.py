from __future__ import annotations

from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True)
class RetrievalMetricResult:
    hit_at_k: float
    recall_at_k: float
    precision_at_k: float
    mrr: float
    source_completeness: float
    section_hit_rate: float
    metadata_filter_accuracy: float
    no_answer_accuracy: float | None


def evaluate_retrieval(
    sources: list[dict[str, Any]],
    expected_doc_ids: set[str] | None = None,
    expected_section_keywords: set[str] | None = None,
    required_keywords: set[str] | None = None,
    filters: dict[str, Any] | None = None,
    is_no_answer_case: bool = False,
    k: int = 5,
) -> RetrievalMetricResult:
    top_sources = sources[:k]
    expected_doc_ids = expected_doc_ids or set()
    expected_section_keywords = expected_section_keywords or set()
    required_keywords = required_keywords or set()
    filters = filters or {}

    relevant_flags = [
        is_relevant_source(source, expected_doc_ids, expected_section_keywords, required_keywords)
        for source in top_sources
    ]
    first_hit_rank = next((idx for idx, hit in enumerate(relevant_flags, start=1) if hit), None)

    expected_units = len(expected_doc_ids) + len(expected_section_keywords)
    matched_units = count_matched_expected_units(top_sources, expected_doc_ids, expected_section_keywords)
    no_answer_accuracy = 1.0 if is_no_answer_case and not sources else 0.0 if is_no_answer_case else None

    return RetrievalMetricResult(
        hit_at_k=1.0 if any(relevant_flags) else 0.0,
        recall_at_k=safe_divide(matched_units, expected_units),
        precision_at_k=safe_divide(sum(1 for hit in relevant_flags if hit), len(top_sources)),
        mrr=0.0 if first_hit_rank is None else round(1.0 / first_hit_rank, 4),
        source_completeness=calculate_source_completeness(top_sources, required_keywords),
        section_hit_rate=calculate_section_hit_rate(top_sources, expected_section_keywords),
        metadata_filter_accuracy=calculate_metadata_filter_accuracy(top_sources, filters),
        no_answer_accuracy=no_answer_accuracy,
    )


def is_relevant_source(
    source: dict[str, Any],
    expected_doc_ids: set[str],
    expected_section_keywords: set[str],
    required_keywords: set[str],
) -> bool:
    text = searchable_text(source)
    doc_hit = bool(expected_doc_ids and str(source.get("document_id")) in expected_doc_ids)
    section_hit = any(keyword.lower() in text for keyword in expected_section_keywords)
    keyword_hit = any(keyword.lower() in text for keyword in required_keywords)
    return doc_hit or section_hit or keyword_hit


def count_matched_expected_units(
    sources: list[dict[str, Any]],
    expected_doc_ids: set[str],
    expected_section_keywords: set[str],
) -> int:
    matched_docs = {
        str(source.get("document_id"))
        for source in sources
        if str(source.get("document_id")) in expected_doc_ids
    }
    text = "\n".join(searchable_text(source) for source in sources)
    matched_sections = {
        keyword
        for keyword in expected_section_keywords
        if keyword.lower() in text
    }
    return len(matched_docs) + len(matched_sections)


def calculate_source_completeness(
    sources: list[dict[str, Any]],
    required_keywords: set[str],
) -> float:
    if not required_keywords:
        return 0.0

    text = "\n".join(searchable_text(source) for source in sources)
    matched_count = sum(1 for keyword in required_keywords if keyword.lower() in text)
    return safe_divide(matched_count, len(required_keywords))


def calculate_section_hit_rate(
    sources: list[dict[str, Any]],
    expected_section_keywords: set[str],
) -> float:
    if not expected_section_keywords:
        return 0.0

    text = "\n".join(
        str(source.get("section_title") or "").lower()
        for source in sources
    )
    matched_count = sum(1 for keyword in expected_section_keywords if keyword.lower() in text)
    return safe_divide(matched_count, len(expected_section_keywords))


def calculate_metadata_filter_accuracy(
    sources: list[dict[str, Any]],
    filters: dict[str, Any],
) -> float:
    effective_filters = {
        key: value
        for key, value in filters.items()
        if value not in {None, ""}
    }
    if not sources or not effective_filters:
        return 0.0

    checks = 0
    matched = 0

    for source in sources:
        metadata = source.get("metadata") or {}
        for key, expected_value in effective_filters.items():
            actual_value = source.get(key, metadata.get(key))
            checks += 1
            matched += int(str(actual_value) == str(expected_value))

    return safe_divide(matched, checks)


def searchable_text(source: dict[str, Any]) -> str:
    return " ".join(
        str(value or "")
        for value in [
            source.get("document_id"),
            source.get("title"),
            source.get("section_title"),
            source.get("content"),
            source.get("source_uri"),
        ]
    ).lower()


def safe_divide(numerator: int | float, denominator: int | float) -> float:
    if denominator == 0:
        return 0.0

    return round(float(numerator) / float(denominator), 4)
