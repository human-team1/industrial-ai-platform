from __future__ import annotations

from datetime import datetime

from config.settings import Settings
from domain.rag.ports import LLMPort, RetrieverPort
from domain.rag.query_models import RagQueryCommand, RagQueryResult, RagSourceResult
from domain.rag.question_policy import (
    DOCUMENT_INTENT_KEYWORDS,
    DOCUMENT_SEARCH_KEYWORDS,
    OUT_OF_SCOPE_KEYWORDS,
    PROMPT_INJECTION_KEYWORDS,
    RESULT_LINKED_KEYWORDS,
    contains_any,
    normalize_question,
)


class RagService:
    def __init__(self, settings: Settings, retriever: RetrieverPort, llm_client: LLMPort) -> None:
        self._settings = settings
        self._retriever = retriever
        self._llm_client = llm_client

    def query(self, request: RagQueryCommand) -> RagQueryResult:
        question = normalize_question(request.question)
        mode = self._classify(question, bool(request.result_context or request.result_id))

        if mode == "OUT_OF_SCOPE":
            return self._result(
                answer="설비 점검, 장애 대응, 매뉴얼 검색 범위의 질문만 답변할 수 있습니다.",
                status="OUT_OF_SCOPE",
                mode=mode,
            )

        if mode == "GENERAL":
            return self._result(
                answer="설비 점검 결과나 문서 기반 조치 방법을 질문해 주세요.",
                status="OUT_OF_SCOPE",
                mode=mode,
            )

        try:
            sources = self._retriever.search(
                query=self._build_retrieval_query(question, request),
                filters={"organization_id": request.organization_id},
                top_k=request.top_k,
            )
        except Exception:
            return self._result(
                answer="문서 검색 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
                status="VECTOR_STORE_FAILED",
                mode=mode,
                errors=["vector_store_failed"],
            )

        if not sources:
            return self._result(
                answer="관련 문서를 찾지 못했습니다. 설비명, 증상, 오류 메시지를 포함해 다시 질문해 주세요.",
                status="NO_RELEVANT_SOURCE",
                mode=mode,
            )

        source_results = [self._to_source_result(source) for source in sources]
        prompt = self._build_prompt(question, request, source_results)
        try:
            llm_result = self._llm_client.generate(prompt=prompt)
        except Exception:
            return self._result(
                answer="답변 생성 중 오류가 발생했습니다. 검색된 출처를 기준으로 매뉴얼을 확인해 주세요.",
                status="LLM_FAILED",
                mode=mode,
                sources=source_results,
                errors=["llm_failed"],
            )

        return self._result(
            answer=llm_result.answer,
            status="ANSWERED",
            mode=mode,
            sources=source_results,
            metadata={
                "llmModel": llm_result.model_name or self._settings.llm_model_name,
                "createdAt": datetime.now().isoformat(),
            },
        )

    def _classify(self, question: str, has_result_context: bool) -> str:
        if contains_any(question, PROMPT_INJECTION_KEYWORDS) or contains_any(question, OUT_OF_SCOPE_KEYWORDS):
            return "OUT_OF_SCOPE"
        if has_result_context or contains_any(question, RESULT_LINKED_KEYWORDS):
            return "RESULT_LINKED"
        if contains_any(question, DOCUMENT_INTENT_KEYWORDS) or contains_any(question, DOCUMENT_SEARCH_KEYWORDS):
            return "DOCUMENT_SEARCH"
        return "GENERAL"

    def _build_retrieval_query(self, question: str, request: RagQueryCommand) -> str:
        context = request.result_context
        if context is None:
            return question
        if isinstance(context, dict):
            parts = [
                question,
                context.get("equipmentName") or context.get("equipment_name"),
                context.get("category"),
                context.get("decisionCode") or context.get("decision"),
                context.get("anomalySummary") or context.get("anomaly_type"),
            ]
            return " ".join(str(part) for part in parts if part)
        parts = [
            question,
            getattr(context, "equipment_name", None),
            getattr(context, "category", None),
            getattr(context, "decision", None),
            getattr(context, "anomaly_type", None),
            getattr(context, "heatmap_location", None),
        ]
        return " ".join(str(part) for part in parts if part)

    def _build_prompt(self, question: str, request: RagQueryCommand, sources: list[RagSourceResult]) -> str:
        source_text = "\n\n".join(
            f"[S{index}] {source.title} {source.section_title or ''} p.{source.page or '-'}\n{source.source_snippet or ''}"
            for index, source in enumerate(sources, start=1)
        )
        result_context = request.result_context
        return (
            "당신은 산업 설비 점검 보조 AI입니다. 반드시 제공된 문서 출처에 근거해 한국어로 답변하세요.\n"
            f"질문: {question}\n"
            f"검사 결과 문맥: {result_context or '없음'}\n"
            f"출처:\n{source_text}\n"
            "답변에는 우선 조치와 확인 순서를 간결히 포함하세요."
        )

    def _to_source_result(self, source) -> RagSourceResult:
        content = getattr(source, "content", None)
        snippet = " ".join(str(content or "").split())[:220] or None
        return RagSourceResult(
            document_id=str(getattr(source, "document_id", "")),
            document_version_id=str(getattr(source, "document_version_id", "")),
            chunk_id=str(getattr(source, "chunk_id", "")),
            title=str(getattr(source, "title", "제목 없음")),
            document_type=getattr(source, "document_type", None),
            section_title=getattr(source, "section_title", None),
            page=getattr(source, "page", None),
            score=getattr(source, "score", None),
            source_snippet=snippet,
            source_uri=getattr(source, "source_uri", None),
        )

    def _result(
        self,
        *,
        answer: str,
        status: str,
        mode: str,
        sources: list[RagSourceResult] | None = None,
        errors: list[str] | None = None,
        metadata: dict | None = None,
    ) -> RagQueryResult:
        merged_metadata = {
            "answerStatus": status,
            "llmModel": self._settings.llm_model_name,
            "createdAt": datetime.now().isoformat(),
            **(metadata or {}),
        }
        return RagQueryResult(
            answer=answer,
            answer_type=status,
            question_mode=mode,
            sources=sources or [],
            errors=errors or [],
            metadata=merged_metadata,
        )
