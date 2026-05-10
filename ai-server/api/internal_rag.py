from fastapi import APIRouter, Depends

from api.schemas import RagQueryRequest, rag_result_to_response
from application.rag.graph_runner import RagGraphRunner
from application.rag.response_mapper import build_rag_query_response
from container.dependencies import get_rag_graph_runner
from domain.rag.state import GraphState

router = APIRouter()


@router.post("/rag/query")
async def query_rag_internal(
    request: RagQueryRequest,
    runner: RagGraphRunner = Depends(get_rag_graph_runner),
) -> dict:
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
            "api_path": "/ai/v1/internal/rag/query",
            "conversation_id": request.conversation_id,
            "top_k": request.top_k,
            "stream": request.stream,
            "debug": request.debug,
        },
    )
    response = rag_result_to_response(build_rag_query_response(result))
    return {
        "success": True,
        "data": response.model_dump(by_alias=True),
        "message": "문서 기반 답변을 생성했습니다.",
    }
