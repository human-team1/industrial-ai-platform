from __future__ import annotations

from pathlib import Path
from string import Template
from typing import Any


PROMPT_FILE_MAP = {
    "prompt_v1_basic": "prompt_v1_basic.txt",
    "prompt_v2_action_grounded": "prompt_v2_action_grounded.txt",
    "prompt_v3_result_safety": "prompt_v3_result_safety.txt",
}


class PromptBuilder:
    """
    RAG 답변 생성을 위한 프롬프트 조립기.

    역할:
    - prompt_version에 맞는 템플릿 로드
    - question / result_context / sources를 텍스트로 정규화
    - LLM에 전달할 최종 prompt 생성
    """

    RESULT_CONTEXT_FIELDS = (
        "result_id",
        "inspection_id",
        "equipment_name",
        "category",
        "decision",
        "anomaly_score",
        "confidence",
        "anomaly_type",
        "heatmap_location",
        "model_version",
        "threshold_profile",
        "artifact_uri",
    )

    SOURCE_FIELD_SPECS = (
        ("title", ("title",)),
        ("document_id", ("document_id", "doc_id")),
        ("document_version_id", ("document_version_id",)),
        ("chunk_id", ("chunk_id",)),
        ("section_title", ("section_title", "section")),
        ("page", ("page",)),
        ("score", ("score",)),
        ("source_uri", ("source_uri",)),
        ("content", ("content",)),
    )

    def __init__(self, prompt_dir: str | Path, prompt_version: str) -> None:
        self.prompt_dir = Path(prompt_dir)
        self.prompt_version = prompt_version

    def build(
        self,
        *,
        question: str,
        question_mode: str,
        result_context: Any | None,
        sources: list[Any],
    ) -> tuple[str, dict[str, Any]]:
        template_text = self._load_template()
        result_context_text = self._format_result_context(result_context)
        sources_text = self._format_sources(sources)

        prompt = Template(template_text).safe_substitute(
            question=question,
            question_mode=question_mode,
            result_context=result_context_text,
            sources=sources_text,
        )

        return prompt, self._build_prompt_inputs(
            question_mode=question_mode,
            result_context=result_context,
            sources=sources,
        )

    def _load_template(self) -> str:
        return self._resolve_template_path().read_text(encoding="utf-8")

    def _resolve_template_path(self) -> Path:
        file_name = PROMPT_FILE_MAP.get(self.prompt_version)

        if file_name is None:
            raise ValueError(f"unsupported prompt_version: {self.prompt_version}")

        path = self.prompt_dir / file_name

        if not path.exists():
            raise FileNotFoundError(f"prompt template not found: {path}")

        return path

    def _format_result_context(self, result_context: Any | None) -> str:
        if result_context is None:
            return "검사 결과 문맥 없음"

        lines = [
            f"- {field_name}: {self._get_value(result_context, field_name)}"
            for field_name in self.RESULT_CONTEXT_FIELDS
        ]

        return "\n".join(lines)

    def _format_sources(self, sources: list[Any]) -> str:
        if not sources:
            return "참고 문서 없음"

        blocks: list[str] = []

        for index, source in enumerate(sources, start=1):
            blocks.append(self._format_source_block(index=index, source=source))

        return "\n\n".join(blocks)

    def _format_source_block(self, *, index: int, source: Any) -> str:
        lines = [f"[S{index}]"]

        for label, candidates in self.SOURCE_FIELD_SPECS:
            value = self._get_first_value(source, *candidates)
            lines.append(f"- {label}: {value}")

        return "\n".join(lines)

    def _extract_source_ids(self, sources: list[Any]) -> list[str]:
        source_ids: list[str] = []

        for source in sources:
            document_id = self._get_value(source, "document_id") or self._get_value(source, "doc_id")
            chunk_id = self._get_value(source, "chunk_id")

            if document_id and chunk_id:
                source_ids.append(f"{document_id}::{chunk_id}")
            elif chunk_id:
                source_ids.append(str(chunk_id))

        return source_ids

    def _build_prompt_inputs(
        self,
        *,
        question_mode: str,
        result_context: Any | None,
        sources: list[Any],
    ) -> dict[str, Any]:
        return {
            "prompt_version": self.prompt_version,
            "question_mode": question_mode,
            "has_result_context": result_context is not None,
            "source_count": len(sources),
            "source_ids": self._extract_source_ids(sources),
        }

    def _get_first_value(self, obj: Any, *keys: str) -> Any:
        for key in keys:
            value = self._get_value(obj, key)
            if value is not None:
                return value
        return None

    def _get_value(self, obj: Any, key: str) -> Any:
        if obj is None:
            return None

        if isinstance(obj, dict):
            return obj.get(key)

        value = getattr(obj, key, None)

        if hasattr(value, "value"):
            return value.value

        return value
