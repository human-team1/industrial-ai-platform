from pydantic import BaseModel, ConfigDict, Field

from domain.anomaly_models import AnomalyInferenceCommand, AnomalyInferenceResult


class AnomalyInferenceRequest(BaseModel):
    equipment_id: str = Field(..., examples=["press-01"])
    sensor_values: dict[str, float] = Field(default_factory=dict)

    def to_command(self) -> AnomalyInferenceCommand:
        return AnomalyInferenceCommand(
            equipment_id=self.equipment_id,
            sensor_values=self.sensor_values,
        )


class AnomalyInferenceResponse(BaseModel):
    model_config = ConfigDict(protected_namespaces=())

    equipment_id: str
    is_anomaly: bool
    score: float
    model_name: str


def anomaly_result_to_response(result: AnomalyInferenceResult) -> AnomalyInferenceResponse:
    return AnomalyInferenceResponse(
        equipment_id=result.equipment_id,
        is_anomaly=result.is_anomaly,
        score=result.score,
        model_name=result.model_name,
    )
