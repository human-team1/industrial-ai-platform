from fastapi import APIRouter, Depends

from application.anomaly_service import AnomalyService
from api.schemas import AnomalyInferenceRequest, AnomalyInferenceResponse, anomaly_result_to_response
from container.dependencies import get_anomaly_service

router = APIRouter()


@router.post("/anomaly", response_model=AnomalyInferenceResponse)
async def infer_anomaly(
    request: AnomalyInferenceRequest,
    service: AnomalyService = Depends(get_anomaly_service),
) -> AnomalyInferenceResponse:
    return anomaly_result_to_response(service.infer(request.to_command()))
