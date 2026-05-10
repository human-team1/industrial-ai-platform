from fastapi import APIRouter, Depends, Request

from api.schemas import InferImageApiResponse, InferImageRequest
from application.vision_inference_service import VisionInferenceService
from container.dependencies import get_vision_inference_service

router = APIRouter()


@router.post("/infer-image", response_model=InferImageApiResponse)
async def infer_image(
    payload: InferImageRequest,
    request: Request,
    service: VisionInferenceService = Depends(get_vision_inference_service),
) -> InferImageApiResponse:
    request_id = getattr(request.state, "request_id", None) or request.headers.get("X-Request-Id") or "generated-request-id"
    return await service.infer_image(payload, request_id)
