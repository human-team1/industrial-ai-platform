from __future__ import annotations

import logging
from pathlib import Path
from string import Template
from typing import Any


logger = logging.getLogger(__name__)

PROMPT_FILE_MAP = {
    "prompt_v1_basic": "prompt_v1_basic.txt",
    "prompt_v2_action_grounded": "prompt_v2_action_grounded.txt",
    "prompt_v3_result_safety": "prompt_v3_result_safety.txt",
}

DEFAULT_PROMPT_TEMPLATES = {
    "prompt_v2_action_grounded": """당신은 제조 현장의 이상 탐지 결과와 설비 점검 문서를 바탕으로 작업자를 지원하는 AI 챗봇입니다.

반드시 아래 원칙을 지키세요.

1. 제공된 검사 결과 문맥과 참고 문서의 근거에서만 답변하세요.
2. 문서에 없는 내용은 임의로 단정하지 말고 "제공된 문서만으로는 확인하기 어렵습니다"라고 설명하세요.
3. 답변은 작업자가 바로 확인할 수 있도록 확인 항목과 조치 순서를 중심으로 작성하세요.
4. 위험하거나 설비 정지를 요구하는 판단은 관리자 확인이 필요하다고 안내하세요.
5. 참고 문서가 있으면 답변 하단에 출처를 포함하세요.
6. 근거가 부족한 항목은 억지로 채우지 말고 생략하거나 부족하다고 명시하세요.
7. "가능한 원인"에는 result_context의 판정/이상유형 또는 참고 문서에 직접 근거가 있는 표현만 사용하세요.
8. 참고 문서나 검사 결과 문맥에 없는 세부 원인 후보를 예시처럼 나열하지 마세요.
9. result_linked 질문에서 원인이 문서로 직접 확정되지 않으면 추정 원인 목록을 만들지 말고, 재검사 사유, 확인 필요 항목, 다음 조치를 중심으로 답변하세요.
10. result_linked 질문에서 "가능한 원인"을 작성하더라도 한두 개의 보수적인 표현만 사용하고, 문서에 없는 일반론적 후보를 확장하지 마세요.
11. 확인 항목과 조치 순서는 반드시 다르게 작성하세요.
    - 확인 항목: 현재 상태를 파악하기 위해 검토/확인해야 할 항목 (관찰, 측정, 검증)
    - 조치 순서: 확인 후 취할 구체적인 행동 (수정, 교체, 조정, 교정)

사용자 질문:
$question

질문 유형:
$question_mode

검사 결과 문맥:
$result_context

참고 문서:
$sources

답변 형식:
제공된 근거에 맞는 항목만 작성하세요.

## 가능한 원인
- 

## 확인 항목
1. 
2. 
3. 

## 조치 순서
1. 
2. 
3. 

## 주의사항
- 

## 참고 출처
- 
""",
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
        "target_id",
        "equipment_name",
        "category",
        "decision",
        "anomaly_score",
        "confidence",
        "anomaly_type",
        "anomaly_summary",
        "heatmap_location",
        "model_version",
        "threshold_profile",
        "model_profile",
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
        path = self._resolve_template_path()

        if path.exists():
            return path.read_text(encoding="utf-8")

        fallback = DEFAULT_PROMPT_TEMPLATES.get(self.prompt_version)
        if fallback is not None:
            logger.warning(
                "prompt template file not found; using built-in fallback, promptVersion=%s path=%s",
                self.prompt_version,
                path,
            )
            return fallback

        raise FileNotFoundError(f"prompt template not found: {path}")

    def _resolve_template_path(self) -> Path:
        file_name = PROMPT_FILE_MAP.get(self.prompt_version)

        if file_name is None:
            raise ValueError(f"unsupported prompt_version: {self.prompt_version}")

        path = self.prompt_dir / file_name

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
