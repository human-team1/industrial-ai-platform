from fastapi import APIRouter, Depends

from api.schemas import RagQueryRequest, RagQueryResponse, rag_result_to_response
from application.rag.graph_runner import RagGraphRunner
from application.rag.response_mapper import build_rag_query_response
from container.dependencies import get_rag_graph_runner
from domain.rag.state import GraphState

router = APIRouter()


@router.post("/query", response_model=RagQueryResponse)
async def query_rag(
    request: RagQueryRequest,
    runner: RagGraphRunner = Depends(get_rag_graph_runner),
) -> RagQueryResponse:
    state = GraphState(
        user_id=request.user_id,
        organization_id=request.organization_id,
        question=request.question,
        result_id=request.result_id,
        result_context=request.result_context,
        prompt_version=request.prompt_version,
        metadata={
            "conversation_id": request.conversation_id,
            "top_k": request.top_k,
            "stream": request.stream,
            "debug": request.debug,
        },
    )
    result = await runner.arun(
        state=state,
        extra_metadata={
            "api_path": "/ai/v1/rag/query",
            "conversation_id": request.conversation_id,
            "top_k": request.top_k,
            "stream": request.stream,
            "debug": request.debug,
        },
    )
    return rag_result_to_response(build_rag_query_response(result))
