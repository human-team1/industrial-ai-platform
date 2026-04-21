from config.settings import Settings
from domain.schemas import AnomalyInferenceRequest, AnomalyInferenceResponse


class AnomalyService:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    def infer(self, request: AnomalyInferenceRequest) -> AnomalyInferenceResponse:
        score = 0.0
        if request.sensor_values:
            score = max(abs(value) for value in request.sensor_values.values())

        return AnomalyInferenceResponse(
            equipment_id=request.equipment_id,
            is_anomaly=score >= 0.95,
            score=score,
            model_name=self._settings.model_name,
        )
