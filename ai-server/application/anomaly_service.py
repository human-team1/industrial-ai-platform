from config.settings import Settings
from domain.anomaly_models import AnomalyInferenceCommand, AnomalyInferenceResult


class AnomalyService:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    def infer(self, request: AnomalyInferenceCommand) -> AnomalyInferenceResult:
        score = 0.0
        if request.sensor_values:
            score = max(abs(value) for value in request.sensor_values.values())

        return AnomalyInferenceResult(
            equipment_id=request.equipment_id,
            is_anomaly=score >= 0.95,
            score=score,
            model_name=self._settings.model_name,
        )
