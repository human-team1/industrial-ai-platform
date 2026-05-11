from __future__ import annotations

from functools import partial
from typing import Any

from langgraph.graph import END, START, StateGraph

from application.rag.nodes import (
    build_answer_prompt,
    build_general_response,
    build_need_clarification_response,
    build_no_source_response,
    build_out_of_scope_response,
    build_result_context_not_found_response,
    build_retrieval_query,
    build_retriever_error_response,
    check_retrieval_result,
    classify_question_mode,
    finalize_response,
    generate_llm_answer,
    load_result_context,
    retrieve_documents,
    route_by_mode,
    select_result_context_route,
    select_retrieval_guard_route,
    select_route,
    validate_input,
    verify_answer,
)
from application.rag.runtime import RagRuntime
from domain.rag.ports import TracePort
from domain.rag.state import GraphState


def build_rag_graph(runtime: RagRuntime):
    graph = StateGraph(GraphState)

    graph.add_node("validate_input", validate_input)
    graph.add_node("classify_question_mode", classify_question_mode)
    graph.add_node("route_by_mode", route_by_mode)

    graph.add_node("load_result_context", partial(load_result_context, runtime=runtime))
    graph.add_node("retrieve_documents", partial(retrieve_documents, runtime=runtime))
    graph.add_node("check_retrieval_result", check_retrieval_result)

    graph.add_node("build_answer_prompt", partial(build_answer_prompt, runtime=runtime))
    graph.add_node("generate_llm_answer", partial(generate_llm_answer, runtime=runtime))

    graph.add_node("build_general_response", build_general_response)
    graph.add_node("build_out_of_scope_response", build_out_of_scope_response)
    graph.add_node(
        "build_need_clarification_response",
        build_need_clarification_response,
    )
    graph.add_node("build_no_source_response", build_no_source_response)
    graph.add_node(
        "build_retriever_error_response",
        build_retriever_error_response,
    )
    graph.add_node(
        "build_result_context_not_found_response",
        build_result_context_not_found_response,
    )
    graph.add_node("build_retrieval_query", build_retrieval_query)
    graph.add_node("verify_answer", partial(verify_answer, runtime=runtime))
    graph.add_node("finalize_response", finalize_response)

    graph.add_edge(START, "validate_input")
    graph.add_edge("validate_input", "classify_question_mode")
    graph.add_edge("classify_question_mode", "route_by_mode")

    graph.add_conditional_edges(
        "route_by_mode",
        select_route,
        {
            "result_linked": "load_result_context",
            "document_search": "load_result_context",
            "general": "build_general_response",
            "out_of_scope": "build_out_of_scope_response",
            "need_clarification": "build_need_clarification_response",
            "finalize": "finalize_response",
        },
    )

    graph.add_edge("retrieve_documents", "check_retrieval_result")

    graph.add_conditional_edges(
        "check_retrieval_result",
        select_retrieval_guard_route,
        {
            "result_linked": "build_answer_prompt",
            "document_search": "build_answer_prompt",
            "no_source": "build_no_source_response",
            "retriever_error": "build_retriever_error_response",
            "validation_error": "finalize_response",
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
    graph.add_edge("build_answer_prompt", "generate_llm_answer")
    graph.add_edge("generate_llm_answer", "verify_answer")
    graph.add_edge("verify_answer", "finalize_response")
    graph.add_edge("finalize_response", END)

    return graph.compile()


class RagGraphRunner:
    def __init__(
        self,
        *,
        graph: Any | None = None,
        tracer: TracePort | None = None,
    ) -> None:
        if graph is None or tracer is None:
            from container.rag_container import (
                create_langsmith_tracer,
                create_rag_runtime,
            )

            runtime = create_rag_runtime()
            graph = graph or build_rag_graph(runtime)
            tracer = tracer or create_langsmith_tracer()

        self.graph = graph
        self.tracer = tracer
        self.tracer.configure_environment()

    async def arun(
        self,
        *,
        state: GraphState,
        query_case_id: str | None = None,
        extra_metadata: dict[str, Any] | None = None,
    ) -> GraphState:
        config = self.tracer.runnable_config(
            state=state,
            query_case_id=query_case_id,
            extra={
                "entrypoint": "rag_graph_runner",
                **(extra_metadata or {}),
            },
            extra_tags=["graph_skeleton"],
        )

        result = await self.graph.ainvoke(state, config=config)
        return GraphState.model_validate(result)

    def run(
        self,
        *,
        state: GraphState,
        query_case_id: str | None = None,
        extra_metadata: dict[str, Any] | None = None,
    ) -> GraphState:
        config = self.tracer.runnable_config(
            state=state,
            query_case_id=query_case_id,
            extra={
                "entrypoint": "rag_graph_runner",
                **(extra_metadata or {}),
            },
            extra_tags=["graph_skeleton"],
        )

        result = self.graph.invoke(state, config=config)
        return GraphState.model_validate(result)
