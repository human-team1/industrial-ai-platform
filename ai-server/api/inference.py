from fastapi import APIRouter, Depends

from application.anomaly_service import AnomalyService
from container.dependencies import get_anomaly_service
from domain.schemas import AnomalyInferenceRequest, AnomalyInferenceResponse

router = APIRouter()


@router.post("/anomaly", response_model=AnomalyInferenceResponse)
async def infer_anomaly(
    request: AnomalyInferenceRequest,
    service: AnomalyService = Depends(get_anomaly_service),
) -> AnomalyInferenceResponse:
    return service.infer(request)
