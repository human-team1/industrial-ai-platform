from dataclasses import dataclass, field


@dataclass(frozen=True, slots=True)
class AnomalyInferenceCommand:
    equipment_id: str
    sensor_values: dict[str, float] = field(default_factory=dict)


@dataclass(frozen=True, slots=True)
class AnomalyInferenceResult:
    equipment_id: str
    is_anomaly: bool
    score: float
    model_name: str
