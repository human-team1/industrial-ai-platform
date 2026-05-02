from __future__ import annotations

from typing import Literal, TypedDict

from langgraph.graph import END, StateGraph
import requests

from config.settings import Settings
from rag.retrievers import RetrieverPort
from rag.schemas import SourceChunk


QuestionMode = Literal["result_linked", "document_search", "general", "out_of_scope"]
NO_RETRIEVAL_RESULT_MESSAGE = (
    "관련 문서를 찾지 못했습니다. 설비명, 증상, 오류 메시지, 검사 결과를 포함해 다시 질문해 주세요."
)
OUT_OF_SCOPE_MESSAGE = (
    "이 서비스는 산업 이상탐지 결과, 설비 매뉴얼, 문서 검색, 조치 절차 범위의 질문만 지원합니다."
)


class RagGraphState(TypedDict, total=False):
    question: str
    top_k: int
    user_id: str | None
    organization_id: str | None
    result_id: str | None
    mode: QuestionMode
    is_valid: bool
    is_service_scope: bool
    needs_document_search: bool
    result_context: dict
    sources: list[SourceChunk]
    prompt: str
    answer: str
    errors: list[str]


class InspectionRagGraph:
    def __init__(
        self,
        settings: Settings,
        retriever: RetrieverPort,
    ) -> None:
        self._settings = settings
        self._retriever = retriever
        self._graph = self._build_graph()

    def invoke(self, question: str, top_k: int) -> RagGraphState:
        initial_state: RagGraphState = {
            "question": question,
            "top_k": top_k,
            "errors": [],
        }
        return self._graph.invoke(initial_state)

    def _build_graph(self):
        graph = StateGraph(RagGraphState)

        graph.add_node("receive_question", self._receive_question)
        graph.add_node("validate_input", self._validate_input)
        graph.add_node("check_result_context", self._check_result_context)
        graph.add_node("load_result_context", self._load_result_context)
        graph.add_node("decide_result_document_search", self._decide_result_document_search)
        graph.add_node("retrieve_result_documents", self._retrieve_result_documents)
        graph.add_node("build_result_sources", self._build_result_sources)
        graph.add_node("build_no_result_context_answer", self._build_no_result_context_answer)
        graph.add_node("check_document_context", self._check_document_context)
        graph.add_node("build_document_query", self._build_document_query)
        graph.add_node("retrieve_documents", self._retrieve_documents)
        graph.add_node("build_document_sources", self._build_document_sources)
        graph.add_node("check_service_scope", self._check_service_scope)
        graph.add_node("build_out_of_scope_answer", self._build_out_of_scope_answer)
        graph.add_node("build_prompt", self._build_prompt)
        graph.add_node("generate_answer", self._generate_answer)
        graph.add_node("postprocess_answer", self._postprocess_answer)
        graph.add_node("build_safe_fallback_answer", self._build_safe_fallback_answer)
        graph.add_node("save_conversation", self._save_conversation)

        graph.set_entry_point("receive_question")
        graph.add_edge("receive_question", "validate_input")
        graph.add_conditional_edges(
            "validate_input",
            self._route_by_mode,
            {
                "result_linked": "check_result_context",
                "document_search": "check_document_context",
                "general": "check_service_scope",
                "out_of_scope": "build_out_of_scope_answer",
            },
        )

        graph.add_conditional_edges(
            "check_result_context",
            self._has_result_context,
            {
                "yes": "load_result_context",
                "no": "build_no_result_context_answer",
            },
        )
        graph.add_edge("load_result_context", "decide_result_document_search")
        graph.add_conditional_edges(
            "decide_result_document_search",
            self._needs_document_search,
            {
                "yes": "retrieve_result_documents",
                "no": "build_prompt",
            },
        )
        graph.add_conditional_edges(
            "retrieve_result_documents",
            self._has_sources,
            {
                "yes": "build_result_sources",
                "no": "build_prompt",
            },
        )
        graph.add_edge("build_result_sources", "build_prompt")
        graph.add_edge("build_no_result_context_answer", "save_conversation")

        graph.add_edge("check_document_context", "build_document_query")
        graph.add_edge("build_document_query", "retrieve_documents")
        graph.add_conditional_edges(
            "retrieve_documents",
            self._has_sources,
            {
                "yes": "build_document_sources",
                "no": "build_safe_fallback_answer",
            },
        )
        graph.add_edge("build_document_sources", "build_prompt")

        graph.add_conditional_edges(
            "check_service_scope",
            self._is_service_scope,
            {
                "yes": "build_prompt",
                "no": "build_out_of_scope_answer",
            },
        )
        graph.add_edge("build_out_of_scope_answer", "save_conversation")

        graph.add_edge("build_prompt", "generate_answer")
        graph.add_edge("generate_answer", "postprocess_answer")
        graph.add_conditional_edges(
            "postprocess_answer",
            self._is_answer_usable,
            {
                "yes": "save_conversation",
                "no": "build_safe_fallback_answer",
            },
        )
        graph.add_edge("build_safe_fallback_answer", "save_conversation")
        graph.add_edge("save_conversation", END)

        return graph.compile()

    def _receive_question(self, state: RagGraphState) -> RagGraphState:
        question = state.get("question", "").strip()
        return {"question": question}

    def _validate_input(self, state: RagGraphState) -> RagGraphState:
        question = state.get("question", "")
        errors = list(state.get("errors", []))
        is_valid = self._settings.question_min_length <= len(question) <= self._settings.question_max_length
        if not is_valid:
            errors.append("QUESTION_LENGTH_INVALID")

        return {
            "is_valid": is_valid,
            "mode": self._detect_mode(question) if is_valid else "out_of_scope",
            "errors": errors,
        }

    def _check_result_context(self, state: RagGraphState) -> RagGraphState:
        has_context = bool(state.get("result_id"))
        return {"result_context": {"available": has_context}}

    def _load_result_context(self, state: RagGraphState) -> RagGraphState:
        # Spring 연동 전까지는 result_id 기반 상세 조회를 하지 않고 state의 값만 보존한다.
        return {"result_context": {"available": True, "result_id": state.get("result_id")}}

    def _decide_result_document_search(self, state: RagGraphState) -> RagGraphState:
        question = state.get("question", "")
        return {"needs_document_search": self._contains_any(question, self._document_search_keywords())}

    def _retrieve_result_documents(self, state: RagGraphState) -> RagGraphState:
        return self._retrieve_with_filters(state)

    def _build_result_sources(self, state: RagGraphState) -> RagGraphState:
        return {"sources": state.get("sources", [])}

    def _build_no_result_context_answer(self, state: RagGraphState) -> RagGraphState:
        return {
            "answer": (
                "검사 결과 컨텍스트가 없어 결과 기반 답변을 만들 수 없습니다. "
                "result_id, user_id, organization_id를 포함해 다시 요청해 주세요."
            )
        }

    def _check_document_context(self, state: RagGraphState) -> RagGraphState:
        return {"question": state.get("question", "")}

    def _build_document_query(self, state: RagGraphState) -> RagGraphState:
        return {"question": state.get("question", "")}

    def _retrieve_documents(self, state: RagGraphState) -> RagGraphState:
        return self._retrieve_with_filters(state)

    def _build_document_sources(self, state: RagGraphState) -> RagGraphState:
        return {"sources": state.get("sources", [])}

    def _check_service_scope(self, state: RagGraphState) -> RagGraphState:
        return {"is_service_scope": self._contains_any(state.get("question", ""), self._service_scope_keywords())}

    def _build_out_of_scope_answer(self, state: RagGraphState) -> RagGraphState:
        return {"answer": OUT_OF_SCOPE_MESSAGE}

    def _build_prompt(self, state: RagGraphState) -> RagGraphState:
        sources = state.get("sources", [])
        source_block = "\n\n".join(
            f"[{source.rank or index}] {source.title} / {source.section_title or source.page}\n{source.content}"
            for index, source in enumerate(sources, start=1)
        )
        prompt = (
            "당신은 산업 현장 설비 점검 보조 시스템입니다.\n"
            "답변은 한국어로 간결하게 작성하고, 근거가 부족하면 부족하다고 말하세요.\n\n"
            f"질문:\n{state.get('question', '')}\n\n"
            f"검색 근거:\n{source_block or '검색된 문서 근거 없음'}\n\n"
            "형식:\n- 요약\n- 확인할 항목\n- 다음 조치\n- 참고 출처"
        )
        return {"prompt": prompt}

    def _generate_answer(self, state: RagGraphState) -> RagGraphState:
        prompt = state.get("prompt", "")
        if self._settings.llm_provider.lower() != "ollama":
            return {"answer": self._template_answer(state)}

        try:
            response = requests.post(
                f"{self._settings.ollama_base_url.rstrip('/')}/api/generate",
                json={
                    "model": self._settings.llm_model_name,
                    "prompt": prompt,
                    "stream": False,
                    "options": {
                        "temperature": self._settings.llm_temperature,
                        "num_predict": self._settings.llm_max_tokens,
                    },
                },
                timeout=self._settings.llm_timeout_seconds,
            )
            response.raise_for_status()
            answer = response.json().get("response", "").strip()
            return {"answer": answer or self._template_answer(state)}
        except Exception as exc:
            errors = list(state.get("errors", []))
            errors.append(f"LLM_CALL_FAILED:{exc.__class__.__name__}")
            return {"answer": self._template_answer(state), "errors": errors}

    def _postprocess_answer(self, state: RagGraphState) -> RagGraphState:
        answer = state.get("answer", "").strip()
        if not state.get("sources") and self._settings.rag_require_source:
            answer = f"{answer}\n\n참고: 현재 검색된 문서 출처가 없어 일반 안내로만 답변했습니다."
        return {"answer": answer}

    def _build_safe_fallback_answer(self, state: RagGraphState) -> RagGraphState:
        return {"answer": NO_RETRIEVAL_RESULT_MESSAGE}

    def _save_conversation(self, state: RagGraphState) -> RagGraphState:
        # 대화 이력 저장소가 연결되면 이 노드에서 Spring/MariaDB 또는 Redis 이력 저장을 호출한다.
        return {"answer": state.get("answer", "")}

    def _route_by_mode(self, state: RagGraphState) -> QuestionMode:
        return state.get("mode", "out_of_scope")

    def _has_result_context(self, state: RagGraphState) -> Literal["yes", "no"]:
        return "yes" if state.get("result_context", {}).get("available") else "no"

    def _needs_document_search(self, state: RagGraphState) -> Literal["yes", "no"]:
        return "yes" if state.get("needs_document_search") else "no"

    def _has_sources(self, state: RagGraphState) -> Literal["yes", "no"]:
        return "yes" if state.get("sources") else "no"

    def _is_service_scope(self, state: RagGraphState) -> Literal["yes", "no"]:
        return "yes" if state.get("is_service_scope") else "no"

    def _is_answer_usable(self, state: RagGraphState) -> Literal["yes", "no"]:
        return "yes" if state.get("answer", "").strip() else "no"

    def _detect_mode(self, question: str) -> QuestionMode:
        lower_question = question.lower()
        if any(token in lower_question for token in ["result_id", "결과 기반", "검사 결과", "해당 결과"]):
            return "result_linked"
        if self._contains_any(question, self._document_search_keywords()):
            return "document_search"
        if self._contains_any(question, self._service_scope_keywords()):
            return "general"
        return "out_of_scope"

    def _retrieve_with_filters(self, state: RagGraphState) -> RagGraphState:
        filters = {
            "document_status": self._settings.rag_default_document_status,
        }
        if self._settings.rag_organization_filter_required:
            filters["organization_id"] = self._settings.rag_default_organization_id

        try:
            sources = self._retriever.search(
                query=state.get("question", ""),
                filters=filters,
                top_k=state.get("top_k", self._settings.retrieval_answer_top_k),
            )
            return {"sources": sources}
        except Exception as exc:
            errors = list(state.get("errors", []))
            errors.append(f"RETRIEVAL_FAILED:{exc.__class__.__name__}")
            return {"sources": [], "errors": errors}

    def _template_answer(self, state: RagGraphState) -> str:
        sources = state.get("sources", [])
        if not sources:
            return NO_RETRIEVAL_RESULT_MESSAGE

        first = sources[0]
        return (
            f"요약: '{first.title}' 문서의 '{first.section_title or first.page}' 내용을 우선 확인하세요.\n"
            "확인할 항목: 설비명, 이상 증상, 판정 점수, 임계값, 최근 조치 이력을 함께 비교하세요.\n"
            "다음 조치: 문서 근거에 맞춰 점검 후 경계 구간이면 재검사 대상으로 기록하세요.\n"
            f"참고 출처: {self._format_source(first)}"
        )

    def _format_source(self, source: SourceChunk) -> str:
        locator = source.section_title or (f"p.{source.page}" if source.page else "위치 미상")
        return f"{source.title} - {locator} ({source.chunk_id})"

    def _document_search_keywords(self) -> list[str]:
        return self._split_keywords(self._settings.document_search_keywords) + [
            "매뉴얼",
            "문서",
            "검색",
            "조치",
            "체크리스트",
            "오류",
            "장애",
            "manual",
            "anomaly",
            "heatmap",
        ]

    def _service_scope_keywords(self) -> list[str]:
        return self._split_keywords(self._settings.service_scope_keywords) + [
            "이상탐지",
            "검사",
            "설비",
            "불량",
            "정상",
            "재검사",
            "threshold",
            "score",
        ]

    def _split_keywords(self, raw_keywords: str) -> list[str]:
        return [keyword.strip().lower() for keyword in raw_keywords.split(",") if keyword.strip()]

    def _contains_any(self, text: str, keywords: list[str]) -> bool:
        lower_text = text.lower()
        return any(keyword in lower_text for keyword in keywords)
