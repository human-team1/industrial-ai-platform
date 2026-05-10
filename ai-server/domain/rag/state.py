from __future__ import annotations

from typing import Any

from pydantic import BaseModel, Field, ConfigDict

from domain.rag.models import (
    AnswerType,
    QuestionMode,
    ResultContext,
    SafetyFlag,
)
from domain.rag.source_chunk import SourceChunk


# LangGraph 노드 사이에서 공유되는 상태.
class GraphState(BaseModel):
    """
    LangGraph 내부에서 노드 간 전달되는 상태.
    v0에서는 Pydantic BaseModel로 시작한다.

    노드는 state를 직접 크게 변경하기보다,
    필요한 필드만 update해서 다음 노드로 넘기는 방식을 권장한다.
    """

    model_config = ConfigDict(extra="ignore", arbitrary_types_allowed=True)

    # requester context
    user_id: int | None = None
    organization_id: int | None = None

    # question
    question: str
    normalized_question: str | None = None
    question_mode: QuestionMode | None = None
    
    query_case_id: str | None = None

    # result-linked context
    result_id: str | None = None
    result_context: ResultContext | None = None

    # control flags
    need_result_context: bool = False
    need_retrieval: bool = False
    need_llm: bool = False
    need_clarification: bool = False

    # retrieval
    retrieval_query: str | None = None
    sources: list[SourceChunk] = Field(default_factory=list)
    retriever_type: str = "mock"
    retriever_called: bool = False
    retrieval_config_id: str | None = None


    # prompt / llm
    prompt: str | None = None
    prompt_version: str | None = None
    prompt_inputs: dict[str, Any] = Field(default_factory=dict)
    
    need_llm: bool = False
    
    llm_called: bool = False
    llm_model_name: str | None = None
    llm_provider: str | None = None
    
    llm_model: str | None = None
    llm_latency_ms: int | None = None
    llm_error_message: str | None = None

    # answer
    answer: str | None = None
    answer_type: AnswerType | None = None
    citation_ok: bool = False

    # safety / validation
    is_out_of_scope: bool = False
    safety_flags: list[SafetyFlag | str] = Field(default_factory=list)
    errors: list[str] = Field(default_factory=list)
    source_warnings: list[str] = Field(default_factory=list)


    # tracing / eval
    trace_id: str | None = None
    graph_version: str = "graph_v0"
    route_path: list[str] = Field(default_factory=list)
    latency_ms: int | None = None

    # debug / extension
    metadata: dict[str, Any] = Field(default_factory=dict)
    

    def add_route(self, node_name: str) -> None:
        self.route_path.append(node_name)

    def add_safety_flag(self, flag: SafetyFlag | str) -> None:
        if flag not in self.safety_flags:
            self.safety_flags.append(flag)

    def add_error(self, message: str) -> None:
        if message not in self.errors:
            self.errors.append(message)
