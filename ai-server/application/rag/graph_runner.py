from __future__ import annotations

from typing import Any

from langgraph.graph import END, START, StateGraph

from application.rag.nodes import (
    build_general_response,
    build_need_clarification_response,
    build_no_source_response,
    build_out_of_scope_response,
    build_result_context_not_found_response,
    build_retriever_error_response,
    build_answer_prompt,
    build_retrieval_query,
    check_retrieval_result,
    classify_question_mode,
    finalize_response,
    load_result_context,
    retrieve_documents,
    route_by_mode,
    select_result_context_route,
    select_retrieval_guard_route,
    select_route,
    validate_input,
    generate_llm_answer,
    verify_answer,
)

from config.settings import settings
from domain.rag.state import GraphState
from infrastructure.tracing.langsmith_tracer import LangSmithTracer


# Step 5 graph skeleton의 노드 연결 순서와 분기 구조를 조립한다.
def build_rag_graph():
    graph = StateGraph(GraphState)

    graph.add_node("validate_input", validate_input)
    graph.add_node("classify_question_mode", classify_question_mode)
    graph.add_node("route_by_mode", route_by_mode)
        
    graph.add_node("load_result_context", load_result_context)
    graph.add_node("retrieve_documents", retrieve_documents)
    graph.add_node("check_retrieval_result", check_retrieval_result)
    
    graph.add_node("build_answer_prompt", build_answer_prompt)
    graph.add_node("generate_llm_answer", generate_llm_answer)
    
    graph.add_node("build_general_response", build_general_response)
    graph.add_node("build_out_of_scope_response", build_out_of_scope_response)
    graph.add_node("build_need_clarification_response", build_need_clarification_response)
    graph.add_node("build_no_source_response", build_no_source_response)
    graph.add_node("build_retriever_error_response", build_retriever_error_response)
    graph.add_node(
        "build_result_context_not_found_response",
        build_result_context_not_found_response,
    )
    graph.add_node("build_retrieval_query", build_retrieval_query)
    graph.add_node("verify_answer", verify_answer)
    graph.add_node("finalize_response", finalize_response)
    
    graph.add_edge(START, "validate_input")
    graph.add_edge("validate_input", "classify_question_mode")
    graph.add_edge("classify_question_mode", "route_by_mode")

    graph.add_conditional_edges(
        "route_by_mode",
        select_route,
        {
            # Step 8부터 result/document 경로는 placeholder 직행 대신 retrieval 노드를 먼저 탄다.
            # Step 9부터 result/document 경로는 retrieval 전에 result_context 로딩을 먼저 시도한다.
            "result_linked": "load_result_context",
            "document_search": "load_result_context",
            "general": "build_general_response",
            "out_of_scope": "build_out_of_scope_response",
            "need_clarification": "build_need_clarification_response",
            "finalize": "finalize_response",
        },
    )
    
    graph.add_edge("retrieve_documents", "check_retrieval_result")

    # retrieval이 성공한 정상 케이스만 Step 10 prompt 생성 경로로 보낸다.
    graph.add_conditional_edges(
        "check_retrieval_result",
        select_retrieval_guard_route,
        {
            # 정상 retrieval 케이스는 아직 prompt/LLM이 없어서 placeholder 응답으로 마무리한다.
            "result_linked": "build_answer_prompt",
            "document_search": "build_answer_prompt",
            "no_source": "build_no_source_response",
            "retriever_error": "build_retriever_error_response",
            "general": "build_general_response",
        },
    )
    
    graph.add_conditional_edges(
        "load_result_context",
        select_result_context_route,
        {
            "continue": "build_retrieval_query",
            "context_not_found": "build_result_context_not_found_response",
        },
    )
    
    graph.add_edge("build_retrieval_query", "retrieve_documents")
    
    graph.add_edge("build_general_response", "finalize_response")
    graph.add_edge("build_out_of_scope_response", "finalize_response")
    graph.add_edge("build_need_clarification_response", "finalize_response")
    graph.add_edge("build_no_source_response", "finalize_response")
    graph.add_edge("build_retriever_error_response", "finalize_response")
    graph.add_edge("build_result_context_not_found_response", "finalize_response")
    # Step 10은 prompt 생성까지만 확인하고 실제 답변 생성은 Step 11에서 이어간다.
    graph.add_edge("build_answer_prompt", "generate_llm_answer")
    graph.add_edge("generate_llm_answer", "verify_answer")
    graph.add_edge("verify_answer", "finalize_response")    
    graph.add_edge("finalize_response", END)
    
    return graph.compile()


class RagGraphRunner:
    def __init__(self) -> None:
        # 그래프와 LangSmith tracer를 한 번만 준비해서 재사용한다.
        self.graph = build_rag_graph()
        self.tracer = LangSmithTracer(settings)
        self.tracer.configure_environment()

    async def arun(
        self,
        *,
        state: GraphState,
        query_case_id: str | None = None,
        extra_metadata: dict[str, Any] | None = None,
    ) -> GraphState:
        # 비동기 실행용 진입점. trace metadata를 붙여 그래프를 실행한다.
        config = self.tracer.runnable_config(
            state=state,
            query_case_id=query_case_id,
            extra={
                "entrypoint": "rag_graph_runner",
                **(extra_metadata or {}),
            },
            extra_tags=["graph_skeleton"],
        )

        result = await self.graph.ainvoke(
            state,
            config=config,
        )

        return GraphState.model_validate(result)

    def run(
        self,
        *,
        state: GraphState,
        query_case_id: str | None = None,
        extra_metadata: dict[str, Any] | None = None,
    ) -> GraphState:
        # 동기 실행용 진입점. test/eval 스크립트에서 주로 사용한다.
        config = self.tracer.runnable_config(
            state=state,
            query_case_id=query_case_id,
            extra={
                "entrypoint": "rag_graph_runner",
                **(extra_metadata or {}),
            },
            extra_tags=["graph_skeleton"],
        )

        result = self.graph.invoke(
            state,
            config=config,
        )

        return GraphState.model_validate(result)
