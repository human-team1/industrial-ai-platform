from __future__ import annotations

import re
from dataclasses import dataclass, field
from typing import Any

from config.settings import settings
from domain.rag.models import SafetyFlag


@dataclass
class SourceVerificationResult:
    citation_ok: bool
    has_sources: bool
    has_citation_section: bool
    has_source_reference: bool
    metadata_ok: bool
    grounded_ok: bool
    low_score_detected: bool
    warnings: list[str] = field(default_factory=list)
    safety_flags: list[SafetyFlag] = field(default_factory=list)


class SourceVerifier:
    """
    LLM 답변 생성 이후 실행되는 rule-based source/citation verifier.

    책임:
    - sources가 있는 답변에서 출처 섹션이 있는지 확인
    - 답변이 최소한 source와 연결되어 있는지 확인
    - source metadata 필수 필드 누락 여부 확인
    - 낮은 score source 감지
    """

    CITATION_SECTION_PATTERNS = [
        r"참고\s*출처",
        r"참고\s*문서",
        r"출처",
        r"References?",
        r"Sources?",
    ]

    STOPWORDS = {
        "그리고",
        "그러나",
        "따라서",
        "대한",
        "통해",
        "경우",
        "확인",
        "항목",
        "조치",
        "순서",
        "가능",
        "문서",
        "제공",
        "결과",
        "기준",
        "내용",
        "필요",
    }

    SOURCE_METADATA_KEYS = (
        ("document_id", ("document_id", "doc_id")),
        ("document_version_id", ("document_version_id",)),
        ("chunk_id", ("chunk_id",)),
        ("title", ("title",)),
        ("section_title_or_page", ("section_title", "section", "page")),
        ("score", ("score",)),
    )

    def verify(
        self,
        *,
        answer: str | None,
        sources: list[Any],
    ) -> SourceVerificationResult:
        answer_text = (answer or "").strip()
        source_list = list(sources or [])

        warnings: list[str] = []
        safety_flags: list[SafetyFlag] = []

        has_sources = len(source_list) > 0

        if not has_sources:
            return self._build_no_sources_result(warnings)

        has_citation_section = self._has_citation_section(answer_text)
        if not has_citation_section:
            warnings.append("citation_section_missing")
            safety_flags.append(SafetyFlag.CITATION_MISSING)

        metadata_ok = self._check_source_metadata(source_list, warnings)
        if not metadata_ok:
            safety_flags.append(SafetyFlag.SOURCE_METADATA_MISSING)

        low_score_detected = self._check_low_score(source_list, warnings)
        if low_score_detected:
            safety_flags.append(SafetyFlag.LOW_SOURCE_SCORE)

        has_source_reference = self._has_source_reference(
            answer=answer_text,
            sources=source_list,
        )
        if not has_source_reference:
            warnings.append("source_reference_missing")
            if SafetyFlag.CITATION_MISSING not in safety_flags:
                safety_flags.append(SafetyFlag.CITATION_MISSING)

        grounded_ok = self._check_grounded_overlap(
            answer=answer_text,
            sources=source_list,
        )
        if not grounded_ok:
            warnings.append("answer_source_overlap_low")
            safety_flags.append(SafetyFlag.UNSUPPORTED_CLAIM)

        citation_ok = (
            has_sources
            and has_citation_section
            and has_source_reference
            and metadata_ok
            and grounded_ok
        )

        return SourceVerificationResult(
            citation_ok=citation_ok,
            has_sources=has_sources,
            has_citation_section=has_citation_section,
            has_source_reference=has_source_reference,
            metadata_ok=metadata_ok,
            grounded_ok=grounded_ok,
            low_score_detected=low_score_detected,
            warnings=warnings,
            safety_flags=self._unique_flags(safety_flags),
        )

    def _has_citation_section(self, answer: str) -> bool:
        return any(
            re.search(pattern, answer, flags=re.IGNORECASE)
            for pattern in self.CITATION_SECTION_PATTERNS
        )

    def _has_source_reference(self, *, answer: str, sources: list[Any]) -> bool:
        if not answer:
            return False

        if re.search(r"\[S\d+\]", answer):
            return True

        if re.search(r"\bS\d+\b", answer):
            return True

        for source in sources[:3]:
            title = self._get(source, "title")
            section = self._get(source, "section_title") or self._get(source, "section")
            chunk_id = self._get(source, "chunk_id")

            for value in [title, section, chunk_id]:
                if value and str(value).strip() and str(value).strip() in answer:
                    return True

        return False

    def _check_source_metadata(
        self,
        sources: list[Any],
        warnings: list[str],
    ) -> bool:
        metadata_ok = True

        for index, source in enumerate(sources[:3], start=1):
            missing_fields = self._collect_missing_metadata_fields(source)

            if missing_fields:
                metadata_ok = False
                warnings.append(
                    f"source_{index}_metadata_missing:{','.join(missing_fields)}"
                )

        return metadata_ok

    def _check_low_score(
        self,
        sources: list[Any],
        warnings: list[str],
    ) -> bool:
        threshold = getattr(settings, "rag_min_score", 0.4)
        low_score_detected = False

        for index, source in enumerate(sources[:3], start=1):
            score = self._get(source, "score")
            if score is None:
                continue

            score_value = self._parse_score(score)
            if score_value is None:
                warnings.append(f"source_{index}_score_invalid:{score}")
                low_score_detected = True
                continue

            if score_value < threshold:
                warnings.append(
                    f"source_{index}_low_score:{score_value}<threshold:{threshold}"
                )
                low_score_detected = True

        return low_score_detected

    def _check_grounded_overlap(self, *, answer: str, sources: list[Any]) -> bool:
        """
        간단한 rule-based overlap 검증.

        NLI 수준의 사실 검증은 아니고,
        답변이 완전히 source와 무관한지 탐지하는 smoke verifier다.
        """
        if not answer:
            return False

        answer_body = self._strip_citation_section(answer)
        answer_tokens = self._tokenize(answer_body)

        source_text_parts = self._collect_grounding_texts(sources[:3])

        source_tokens = self._tokenize(" ".join(source_text_parts))

        if not answer_tokens or not source_tokens:
            return False

        overlap = answer_tokens & source_tokens

        # 너무 엄격하게 잡지 않는다. 완전 무관 답변만 잡는 수준.
        return len(overlap) >= 2

    def _tokenize(self, text: str) -> set[str]:
        normalized = re.sub(r"[^0-9a-zA-Z가-힣]+", " ", text.lower())
        tokens = {
            token
            for token in normalized.split()
            if len(token) >= 2 and token not in self.STOPWORDS
        }
        return tokens

    def _build_no_sources_result(
        self,
        warnings: list[str],
    ) -> SourceVerificationResult:
        warnings.append("no_sources_for_verification")
        return SourceVerificationResult(
            citation_ok=False,
            has_sources=False,
            has_citation_section=False,
            has_source_reference=False,
            metadata_ok=False,
            grounded_ok=False,
            low_score_detected=False,
            warnings=warnings,
            safety_flags=[SafetyFlag.NO_SOURCES],
        )

    def _collect_missing_metadata_fields(self, source: Any) -> list[str]:
        missing_fields: list[str] = []

        for label, candidates in self.SOURCE_METADATA_KEYS:
            if not self._has_any_value(source, *candidates):
                missing_fields.append(label)

        return missing_fields

    def _collect_grounding_texts(self, sources: list[Any]) -> list[str]:
        source_text_parts: list[str] = []

        for source in sources:
            source_text_parts.extend(
                [
                    str(self._get(source, "title") or ""),
                    str(
                        self._get(source, "section_title")
                        or self._get(source, "section")
                        or ""
                    ),
                    str(self._get(source, "content") or ""),
                ]
            )

        return source_text_parts

    def _parse_score(self, score: Any) -> float | None:
        try:
            return float(score)
        except (TypeError, ValueError):
            return None

    def _get(self, source: Any, key: str) -> Any:
        if isinstance(source, dict):
            return source.get(key)

        value = getattr(source, key, None)
        if value is not None:
            return value

        if hasattr(source, "model_dump"):
            return source.model_dump().get(key)

        if hasattr(source, "dict"):
            return source.dict().get(key)

        return None

    def _has_any_value(self, source: Any, *keys: str) -> bool:
        for key in keys:
            if self._get(source, key) is not None:
                return True
        return False

    def _unique_flags(self, flags: list[SafetyFlag]) -> list[SafetyFlag]:
        result: list[SafetyFlag] = []
        for flag in flags:
            if flag not in result:
                result.append(flag)
        return result
    
    def _strip_citation_section(self, answer: str) -> str:
        """
        답변 본문 grounded 검사 시 참고 출처 섹션은 제외한다.
        출처 제목/문서명이 overlap을 인위적으로 만들 수 있기 때문이다.
        """
        if not answer:
            return ""

        lines = answer.splitlines()
        kept_lines: list[str] = []

        in_citation_section = False
        for line in lines:
            stripped = line.strip()

            if re.match(r"^#{1,6}\s*(참고\s*출처|참고\s*문서|출처|References?|Sources?)\s*$", stripped, flags=re.IGNORECASE):
                in_citation_section = True
                continue

            # 출처 섹션 안에서 다음 헤더가 나오면 다시 본문으로 본다.
            if in_citation_section and re.match(r"^#{1,6}\s+", stripped):
                in_citation_section = False

            if not in_citation_section:
                kept_lines.append(line)

        return "\n".join(kept_lines).strip()


