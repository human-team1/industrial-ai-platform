import logging
from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse

from api.rag_streaming import stream_rag_response
from api.schemas import RagQueryRequest, rag_result_to_response
from application.rag.graph_runner import RagGraphRunner
from application.rag.response_mapper import build_rag_query_response
from container.dependencies import get_rag_graph_runner
from domain.rag.models import ResultContext
from domain.rag.state import GraphState

router = APIRouter()
logger = logging.getLogger(__name__)


def _build_result_context(data: dict | None) -> ResultContext | None:
    if not data:
        return None

    result_context = ResultContext(
        result_id=str(data.get("resultId") or data.get("result_id")) if data.get("resultId") or data.get("result_id") else None,
        inspection_id=str(data.get("inspectionId") or data.get("inspection_id")) if data.get("inspectionId") or data.get("inspection_id") else None,
        decision=data.get("decisionCode") or data.get("decision"),
        score=data.get("score"),
        confidence=data.get("confidence"),
        equipment_name=data.get("equipmentName") or data.get("equipment_name"),
        category=data.get("category"),
        anomaly_score=data.get("anomalyScore") or data.get("anomaly_score") or data.get("score"),
        anomaly_type=data.get("anomalyType") or data.get("anomaly_type"),
        heatmap_location=data.get("heatmapLocation") or data.get("heatmap_location"),
        model_version=data.get("modelVersion") or data.get("model_version"),
        threshold_profile=data.get("thresholdProfile") or data.get("threshold_profile") or data.get("imageThreshold") or data.get("image_threshold"),
        model_profile=data.get("modelProfile") or data.get("model_profile"),
        artifact_uri=data.get("artifactUri") or data.get("artifact_uri"),
        artifact_available=data.get("artifactAvailable") or data.get("artifact_available") or False,
    )
    return result_context


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
        result_context=_build_result_context(request.result_context),
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
    if request.stream:
        return StreamingResponse(
            stream_rag_response(response),
            media_type="text/event-stream",
            headers={"Cache-Control": "no-cache"},
        )
    return {
        "success": True,
        "data": response.model_dump(by_alias=True),
        "message": "문서 기반 답변을 생성했습니다.",
    }
