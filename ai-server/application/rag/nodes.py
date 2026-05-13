from __future__ import annotations

from typing import Any

from application.rag.runtime import RagRuntime
from application.rag.state_helpers import (
    append_route,
    compact_unique_text,
    get_context_value,
    normalize_organization_id_for_retrieval,
    normalize_retrieval_sources,
    to_state,
)
from domain.rag.models import AnswerType, QuestionMode, SafetyFlag
from domain.rag.question_policy import (
    DOCUMENT_INTENT_KEYWORDS,
    DOCUMENT_SEARCH_KEYWORDS,
    OUT_OF_SCOPE_KEYWORDS,
    PROMPT_INJECTION_KEYWORDS,
    RESULT_LINKED_KEYWORDS,
    contains_any,
    normalize_question,
)
from domain.rag.state import GraphState

# dict 입력도 그래프에서 같은 상태 객체로 다루기 위한 변환 헬퍼.
# 어떤 노드를 거쳤는지 route_path에 누적하기 위한 헬퍼.
# 공백 차이를 줄여 질문 분류를 안정적으로 맞추기 위한 정규화.
# 질문 안에 특정 의도 키워드가 있는지 확인한다.
# 질문 길이와 기본 입력 형식을 확인한다.
def validate_input(state: GraphState | dict[str, Any]) -> dict[str, Any]:
    current = to_state(state)
    route_path = append_route(current, "validate_input")

    question = current.question or ""
    normalized = normalize_question(question)
    # organization_id validation is handled only for retrieval paths.

    if len(normalized) < 2:
        return {
            "normalized_question": normalized,
            "answer": "질문이 너무 짧습니다. 설비명, 검사 결과, 증상 등을 포함해 다시 질문해주세요.",
            "answer_type": AnswerType.VALIDATION_ERROR,
            "need_clarification": True,
            "need_llm": False,
            "llm_called": False,
            "route_path": route_path,
            "errors": [*current.errors, "question_too_short"],
        }

    if len(normalized) > 1000:
        return {
            "normalized_question": normalized,
            "answer": "질문이 너무 깁니다. 핵심 증상과 확인하고 싶은 내용을 짧게 정리해 다시 질문해주세요.",
            "answer_type": AnswerType.VALIDATION_ERROR,
            "need_clarification": True,
            "need_llm": False,
            "llm_called": False,
            "route_path": route_path,
            "errors": [*current.errors, "question_too_long"],
        }

    return {
        "normalized_question": normalized,
        "route_path": route_path,
    }


# 질문을 result/document/general/out_of_scope 중 하나로 분류한다.
def classify_question_mode(state: GraphState | dict[str, Any]) -> dict[str, Any]:
    current = to_state(state)
    route_path = append_route(current, "classify_question_mode")

    question = current.normalized_question or current.question
    safety_flags = list(current.safety_flags)

    if contains_any(
        question,
        PROMPT_INJECTION_KEYWORDS,
    ):
        if SafetyFlag.PROMPT_INJECTION not in safety_flags:
            safety_flags.append(SafetyFlag.PROMPT_INJECTION)

        return {
            "question_mode": QuestionMode.OUT_OF_SCOPE,
            "is_out_of_scope": True,
            "need_llm": False,
            "llm_called": False,
            "safety_flags": safety_flags,
            "route_path": route_path,
        }

    if contains_any(
        question,
        OUT_OF_SCOPE_KEYWORDS,
    ):
        return {
            "question_mode": QuestionMode.OUT_OF_SCOPE,
            "is_out_of_scope": True,
            "need_llm": False,
            "llm_called": False,
            "route_path": route_path,
        }

    # 문서/조치 의도가 있으면 result_id가 있어도 document_search를 우선한다.
    # 예: "조명 조건 의심이면 어떻게 재촬영해야 해?" + result_id 있음
    # → result_context는 참고하되, 경로는 document_search
    has_document_intent = (
        contains_any(
            question,
            DOCUMENT_INTENT_KEYWORDS,
        )
        or contains_any(
            question,
            DOCUMENT_SEARCH_KEYWORDS,
        )
    )
    has_result_intent = contains_any(
        question,
        RESULT_LINKED_KEYWORDS,
    )

    # 1) "이 결과", "정상 판정", "좌측 상단"처럼 특정 검사 결과를 가리키는 표현은
    #    조치/세척 같은 단어가 있어도 result_linked를 우선한다.
    if has_result_intent:
        return {
            "question_mode": QuestionMode.RESULT_LINKED,
            "need_result_context": True,
            "route_path": route_path,
        }

    # 2) 점검/조치/절차/확인 항목/재촬영/출처처럼 문서 또는 SOP가 필요한 질문은 document_search.
    if has_document_intent:
        return {
            "question_mode": QuestionMode.DOCUMENT_SEARCH,
            "need_retrieval": True,
            "need_result_context": bool(current.result_id),
            "route_path": route_path,
        }

    # 3) 명시적 결과 표현은 없지만 result_id가 붙어 들어온 경우는 결과 연계 질문으로 처리.
    if current.result_id:
        return {
            "question_mode": QuestionMode.RESULT_LINKED,
            "need_result_context": True,
            "route_path": route_path,
        }

    if "챗봇" in question or "뭘 할 수" in question or "사용법" in question:
        return {
            "question_mode": QuestionMode.GENERAL,
            "need_llm": False,
            "route_path": route_path,
        }

    return {
        "question_mode": QuestionMode.GENERAL,
        "need_llm": False,
        "route_path": route_path,
    }


# 분기 직전까지의 route_path만 기록하는 얇은 중간 노드.
def route_by_mode(state: GraphState | dict[str, Any]) -> dict[str, Any]:
    current = to_state(state)
    return {
        "route_path": append_route(current, "route_by_mode"),
    }


# 현재 state를 보고 다음 그래프 노드 이름을 결정한다.
def select_route(state: GraphState | dict[str, Any]) -> str:
    current = to_state(state)

    if current.answer_type == AnswerType.VALIDATION_ERROR:
        return "finalize"

    if current.question_mode == QuestionMode.RESULT_LINKED:
        if not current.result_id:
            return "need_clarification"
        return "result_linked"

    if current.question_mode == QuestionMode.DOCUMENT_SEARCH:
        return "document_search"

    if current.question_mode == QuestionMode.OUT_OF_SCOPE:
        return "out_of_scope"

    return "general"


# 결과 연계 질문인데 result_id가 없을 때 제한 응답을 만든다.
def build_need_clarification_response(state: GraphState | dict[str, Any]) -> dict[str, Any]:
    current = to_state(state)

    return {
        "answer": "검사 결과 설명을 위해 result_id가 필요합니다. 결과 상세 화면에서 다시 질문하거나 result_id를 함께 전달해주세요.",
        "answer_type": AnswerType.NEED_RESULT_CONTEXT,
        "need_clarification": True,
        "need_llm": False,
        "llm_called": False,
        "route_path": append_route(current, "build_need_clarification_response"),
    }


# 서비스 범위 안의 일반 안내 질문에 대한 고정 응답이다.
def build_general_response(state: GraphState | dict[str, Any]) -> dict[str, Any]:
    current = to_state(state)

    return {
        "answer": "이 챗봇은 이상 탐지 결과 설명, 설비 점검 문서 검색, 대응 절차 안내를 지원합니다.",
        "answer_type": AnswerType.GENERAL,
        "need_llm": False,
        "llm_called": False,
        "route_path": append_route(current, "build_general_response"),
    }


# 범위 밖 질문이나 차단 대상 질문에 대한 제한 응답이다.
def build_out_of_scope_response(state: GraphState | dict[str, Any]) -> dict[str, Any]:
    current = to_state(state)

    safety_flags = list(current.safety_flags)

    if SafetyFlag.PROMPT_INJECTION in safety_flags:
        answer = (
            "요청하신 내용은 시스템 지시 무시 또는 내부 프롬프트 노출과 관련되어 처리할 수 없습니다. "
            "이 서비스는 산업 이상탐지 결과, 설비 점검, 문서 검색, 대응 절차 안내 범위의 질문만 지원합니다."
        )
    else:
        answer = (
            "이 서비스는 산업 이상탐지 결과, 설비 점검, 문서 검색, 대응 절차 안내 범위의 질문만 지원합니다."
        )

    return {
        "answer": answer,
        "answer_type": AnswerType.OUT_OF_SCOPE,
        "is_out_of_scope": True,
        "need_llm": False,
        "llm_called": False,
        "route_path": append_route(current, "build_out_of_scope_response"),
    }
    
# retriever는 호출됐지만 관련 문서를 찾지 못했을 때의 제한 응답이다.
def build_no_source_response(state: GraphState | dict[str, Any]) -> dict[str, Any]:
    current = to_state(state)

    return {
        "answer": (
            "관련 문서를 찾지 못했습니다. "
            "설비명, 증상, 오류 메시지, 검사 결과 또는 확인하려는 문서명을 포함해 다시 질문해주세요."
        ),
        "answer_type": AnswerType.NO_RETRIEVAL_RESULT,
        "need_clarification": True,
        "need_llm": False,
        "llm_called": False,
        "retriever_called": current.retriever_called,
        "route_path": append_route(current, "build_no_source_response"),
        "errors": [*current.errors, "no_retrieval_result"],
    }


# retriever 실행 중 예외가 났을 때의 오류 응답이다.
def build_retriever_error_response(state: GraphState | dict[str, Any]) -> dict[str, Any]:
    current = to_state(state)
    
    # 같은 retriever_error가 이미 기록된 경우 중복 추가를 막는다.
    errors = list(current.errors)
    if "retriever_error" not in errors:
        errors.append("retriever_error")

    return {
        "answer": (
            "문서 검색 중 오류가 발생했습니다. "
            "잠시 후 다시 시도하거나 설비명, 증상, 문서명을 더 구체적으로 입력해주세요."
        ),
        "answer_type": AnswerType.RETRIEVER_ERROR,
        "need_clarification": True,
        "need_llm": False,
        "llm_called": False,
        "retriever_called": current.retriever_called,
        "route_path": append_route(current, "build_retriever_error_response"),
        "errors": errors,
    }    
    

#  최종 노드: 경로 기록과 graph version만 정리한다.
def finalize_response(state: GraphState | dict[str, Any]) -> dict[str, Any]:
    current = to_state(state)

    return {
        "route_path": append_route(current, "finalize_response"),
        "graph_version": "graph_v0",
    }

def load_result_context(
    state: GraphState | dict[str, Any],
    *,
    runtime: RagRuntime,
) -> dict[str, Any]:
    current = to_state(state)
    route_path = append_route(current, "load_result_context")

    # Spring에서 이미 result_context를 전달했으면 그것을 사용한다.
    if current.result_context is not None:
        return {
            "need_result_context": True,
            "route_path": route_path,
        }

    # result_id가 없는 질문은 result_context 없이 계속 진행한다.
    if not current.result_id:
        return {
            "route_path": route_path,
        }

    # Mock store에서 result_id 기준 문맥을 조회한다.
    result_context = runtime.result_context_store.get_by_result_id(current.result_id)

    if result_context is None:
        # 잘못된 result_id면 retrieval 전에 clarification 응답으로 안전하게 멈춘다.
        safety_flags = list(current.safety_flags)

        if SafetyFlag.RESULT_CONTEXT_NOT_FOUND not in safety_flags:
            safety_flags.append(SafetyFlag.RESULT_CONTEXT_NOT_FOUND)

        errors = list(current.errors)
        if "result_context_not_found" not in errors:
            errors.append("result_context_not_found")

        return {
            "answer_type": AnswerType.NEED_RESULT_CONTEXT,
            "answer": (
                "요청한 result_id에 해당하는 검사 결과 문맥을 찾지 못했습니다. "
                "결과 상세 화면에서 다시 질문하거나 올바른 result_id를 전달해주세요."
            ),
            "result_context": None,
            "need_clarification": True,
            "need_llm": False,
            "llm_called": False,
            "safety_flags": safety_flags,
            "errors": errors,
            "route_path": route_path,
        }

    return {
        # 찾은 result_context는 이후 retrieval/prompt 단계에서 함께 참조된다.
        "result_context": result_context,
        "need_result_context": True,
        "route_path": route_path,
    }


def select_result_context_route(state: GraphState | dict[str, Any]) -> str:
    current = to_state(state)

    # result_context 조회 실패 시에는 전용 응답 노드로, 아니면 retrieval 단계로 보낸다.
    if "result_context_not_found" in current.errors:
        return "context_not_found"

    return "continue"


def build_result_context_not_found_response(
    state: GraphState | dict[str, Any],
) -> dict[str, Any]:
    current = to_state(state)

    return {
        "answer": (
            current.answer
            or "요청한 검사 결과 문맥을 찾지 못했습니다. result_id를 확인한 뒤 다시 시도해주세요."
        ),
        "answer_type": AnswerType.NEED_RESULT_CONTEXT,
        "need_clarification": True,
        "need_llm": False,
        "llm_called": False,
        "route_path": append_route(
            current,
            "build_result_context_not_found_response",
        ),
    }
    

def build_retrieval_query(state: GraphState | dict[str, Any]) -> dict[str, Any]:
    """
    실제 Retriever에 전달할 검색용 query를 구성한다.

    - result_context가 없으면 사용자 질문 그대로 사용
    - result_context가 있으면 설비/품목/판정/이상유형/위치 정보를 섞어 검색 품질을 보강
    """
    current = to_state(state)
    route_path = append_route(current, "build_retrieval_query")

    question = current.normalized_question or current.question

    if current.result_context is None:
        return {
            "retrieval_query": question,
            "route_path": route_path,
        }

    decision = get_context_value(current.result_context, "decision")
    equipment_name = get_context_value(
        current.result_context,
        "equipment_name",
    )
    category = get_context_value(current.result_context, "category")
    anomaly_type = get_context_value(
        current.result_context,
        "anomaly_type",
    )
    anomaly_summary = get_context_value(
        current.result_context,
        "anomaly_summary",
    )
    heatmap_location = get_context_value(
        current.result_context,
        "heatmap_location",
    )
    model_version = get_context_value(current.result_context, "model_version")
    model_profile = get_context_value(current.result_context, "model_profile")

    # decision을 한글과 함께 검색 쿼리에 포함
    if decision == "DEFECT":
        decision_enhanced = "DEFECT 결함"
    elif decision == "REVIEW_REQUIRED":
        decision_enhanced = "REVIEW_REQUIRED 재검사"
    else:
        decision_enhanced = decision

    extra_terms: list[str] = [
        "점검",
        "조치",
        "대응 절차",
    ]

    if str(decision) == "재검사":
        extra_terms.extend(
            [
                "재검사 기준",
                "촬영 조건",
                "조명",
                "제품 위치",
                "표면 상태",
            ]
        )

    if anomaly_type and "오염" in str(anomaly_type):
        extra_terms.extend(
            [
                "표면 오염",
                "세척",
                "이물질",
                "렌즈 오염",
            ]
        )

    if anomaly_type and "정렬" in str(anomaly_type):
        extra_terms.extend(
            [
                "정렬",
                "가이드 레일",
                "체인 장력",
                "위치 센서",
            ]
        )

    if anomaly_type and "조명" in str(anomaly_type):
        extra_terms.extend(
            [
                "조명 조건",
                "조도",
                "반사",
                "재촬영",
                "렌즈",
            ]
        )

    retrieval_query = compact_unique_text(
        [
            question,
            equipment_name,
            category,
            decision_enhanced,
            anomaly_type,
            anomaly_summary,
            heatmap_location,
            model_version,
            model_profile,
            *extra_terms,
        ]
    )

    return {
        "retrieval_query": retrieval_query,
        "route_path": route_path,
    }

def retrieve_documents(
    state: GraphState | dict[str, Any],
    *,
    runtime: RagRuntime,
) -> dict[str, Any]:
    current = to_state(state)
    route_path = append_route(current, "retrieve_documents")

    query = (
        current.retrieval_query
        or current.normalized_question
        or current.question
    )

    filters = {
        "organization_id": normalize_organization_id_for_retrieval(current.organization_id),
        "document_status": runtime.rag_default_document_status,
    }

    if filters["organization_id"] is None:
        errors = list(current.errors)
        if "organization_id_missing" not in errors:
            errors.append("organization_id_missing")

        safety_flags = list(current.safety_flags)
        if SafetyFlag.VALIDATION_ERROR not in safety_flags:
            safety_flags.append(SafetyFlag.VALIDATION_ERROR)

        return {
            "sources": [],
            "answer": "조직 정보가 누락되어 문서 검색을 진행할 수 없습니다. 다시 로그인한 뒤 시도해주세요.",
            "answer_type": AnswerType.VALIDATION_ERROR,
            "need_clarification": True,
            "need_llm": False,
            "llm_called": False,
            "retriever_called": False,
            "errors": errors,
            "safety_flags": safety_flags,
            "route_path": route_path,
        }

    try:
        raw_result = runtime.retriever.search(
            query=query,
            top_k=runtime.rag_top_k,
            query_case_id=current.query_case_id,
            filters=filters,
        )

        sources = normalize_retrieval_sources(raw_result)

        return {
            "sources": sources,
            "retriever_called": True,
            "retriever_type": runtime.retriever_type,
            "retrieval_config_id": runtime.retrieval_config_id,
            "route_path": route_path,
        }

    except Exception:
        errors = list(current.errors)
        if "retriever_error" not in errors:
            errors.append("retriever_error")

        safety_flags = list(current.safety_flags)
        if SafetyFlag.RETRIEVER_ERROR not in safety_flags:
            safety_flags.append(SafetyFlag.RETRIEVER_ERROR)

        return {
            "sources": [],
            "retriever_called": True,
            "retriever_type": runtime.retriever_type,
            "need_llm": False,
            "llm_called": False,
            "errors": errors,
            "safety_flags": safety_flags,
            "route_path": route_path,
        }
        
        
# retrieval 이후 결과 유무와 오류 여부만 먼저 판단하는 Guard 노드다.
def check_retrieval_result(state: GraphState | dict[str, Any]) -> dict[str, Any]:
    current = to_state(state)
    route_path = append_route(current, "check_retrieval_result")

    # retriever 내부 오류가 기록돼 있으면 retriever_error 응답 경로로 넘긴다.
    if "retriever_error" in current.errors:
        return {
            "route_path": route_path,
            "answer_type": AnswerType.RETRIEVER_ERROR,
            "need_llm": False,
            "llm_called": False,
        }

    if current.answer_type == AnswerType.VALIDATION_ERROR:
        return {
            "route_path": route_path,
            "answer_type": AnswerType.VALIDATION_ERROR,
            "need_llm": False,
            "llm_called": False,
        }

    # 검색은 했지만 source가 비어 있으면 no_retrieval_result로 종료한다.
    if current.need_retrieval and not current.sources:
        return {
            "route_path": route_path,
            "answer_type": AnswerType.NO_RETRIEVAL_RESULT,
            "need_llm": False,
            "llm_called": False,
        }

    return {
        "route_path": route_path,
    }
    
    
def build_answer_prompt(
    state: GraphState | dict[str, Any],
    *,
    runtime: RagRuntime,
) -> dict[str, Any]:
    current = to_state(state)
    route_path = append_route(current, "build_answer_prompt")

    # retrieval / result_context가 준비된 state를
    # LLM 호출 직전의 prompt 상태로 정리한다.
    question = current.normalized_question or current.question

    builder = runtime.prompt_builder_factory(
        current.prompt_version or runtime.prompt_version
    )

    prompt, prompt_inputs = builder.build(
        question=question,
        question_mode=current.question_mode.value if hasattr(current.question_mode, "value") else str(current.question_mode),
        result_context=current.result_context,
        sources=current.sources[:3],
    )

    if current.question_mode == QuestionMode.RESULT_LINKED:
        answer_type = AnswerType.RESULT_LINKED
    elif current.question_mode == QuestionMode.DOCUMENT_SEARCH:
        answer_type = AnswerType.DOCUMENT_SEARCH
    else:
        answer_type = current.answer_type

    return {
        "prompt": prompt,
        "prompt_version": current.prompt_version or runtime.prompt_version,
        "prompt_inputs": prompt_inputs,
        "answer_type": answer_type,
        "need_llm": True,
        "llm_called": False,
        "route_path": route_path,
    }


# retrieval Guard 결과에 따라 다음 응답 노드로 분기한다.
def select_retrieval_guard_route(state: GraphState | dict[str, Any]) -> str:
    current = to_state(state)

    # retrieval 이후에는 오류/빈 결과를 먼저 처리하고, 정상 케이스만 placeholder로 넘긴다.
    if current.answer_type == AnswerType.RETRIEVER_ERROR:
        return "retriever_error"

    if current.answer_type == AnswerType.VALIDATION_ERROR:
        return "validation_error"

    if current.answer_type == AnswerType.NO_RETRIEVAL_RESULT:
        return "no_source"

    if current.question_mode == QuestionMode.RESULT_LINKED:
        return "result_linked"

    if current.question_mode == QuestionMode.DOCUMENT_SEARCH:
        return "document_search"

    return "general"



def generate_llm_answer(
    state: GraphState | dict[str, Any],
    *,
    runtime: RagRuntime,
) -> dict[str, Any]:
    current = to_state(state)
    route_path = append_route(current, "generate_llm_answer")

    if not current.prompt:
        errors = list(current.errors)
        if "prompt_missing" not in errors:
            errors.append("prompt_missing")

        return {
            "answer": "답변 생성을 위한 프롬프트가 없습니다. 질문을 다시 시도해주세요.",
            "answer_type": AnswerType.SYSTEM_ERROR,
            "need_llm": False,
            "llm_called": False,
            "llm_error_message": "prompt_missing",
            "errors": errors,
            "route_path": route_path,
        }

    if runtime.llm_provider != "ollama":
        errors = list(current.errors)
        if "unsupported_llm_provider" not in errors:
            errors.append("unsupported_llm_provider")

        return {
            "answer": f"지원하지 않는 LLM provider입니다: {runtime.llm_provider}",
            "answer_type": AnswerType.SYSTEM_ERROR,
            "need_llm": False,
            "llm_called": False,
            "llm_provider": runtime.llm_provider,
            "llm_error_message": "unsupported_llm_provider",
            "errors": errors,
            "route_path": route_path,
        }

    try:
        result = runtime.llm_client.generate(prompt=current.prompt)

        return {
            "answer": result.answer,
            "need_llm": False,
            "llm_called": True,
            "llm_provider": runtime.llm_provider,
            "llm_model": result.model_name,
            "llm_latency_ms": result.latency_ms,
            "route_path": route_path,
        }

    except Exception as exc:
        errors = list(current.errors)
        if "llm_error" not in errors:
            errors.append("llm_error")

        safety_flags = list(current.safety_flags)
        if SafetyFlag.LLM_ERROR not in safety_flags:
            safety_flags.append(SafetyFlag.LLM_ERROR)

        return {
            "answer": (
                "답변 생성 중 오류가 발생했습니다. "
                "잠시 후 다시 시도하거나 질문을 더 구체적으로 입력해주세요."
            ),
            "answer_type": AnswerType.SYSTEM_ERROR,
            "need_llm": False,
            "llm_called": True,
            "llm_provider": runtime.llm_provider,
            "llm_model": runtime.llm_model_name,
            "llm_error_message": str(exc),
            "safety_flags": safety_flags,
            "errors": errors,
            "route_path": route_path,
        }
        
        
def verify_answer(
    state: GraphState | dict[str, Any],
    *,
    runtime: RagRuntime,
) -> dict[str, Any]:
    """
    LLM 생성 답변의 source/citation 상태를 검증한다.

    주의:
    - sources 없음으로 LLM 차단하는 역할은 여기서 하지 않는다.
    - 검색 실패는 check_retrieval_result에서 처리한다.
    """
    current = to_state(state)
    route_path = append_route(current, "verify_answer")

    # LLM 자체가 실패한 경우에는 citation 검증을 의미 있게 수행하지 않는다.
    if "llm_error" in current.errors:
        return {
            "citation_ok": False,
            "source_warnings": list(current.source_warnings),
            "route_path": route_path,
        }

    result = runtime.source_verifier.verify(
        answer=current.answer,
        sources=current.sources,
    )

    safety_flags = list(current.safety_flags)
    for flag in result.safety_flags:
        if flag not in safety_flags:
            safety_flags.append(flag)

    source_warnings = list(current.source_warnings)
    for warning in result.warnings:
        if warning not in source_warnings:
            source_warnings.append(warning)

    return {
        "citation_ok": result.citation_ok,
        "safety_flags": safety_flags,
        "source_warnings": source_warnings,
        "route_path": route_path,
    }
