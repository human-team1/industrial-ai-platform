from pydantic import BaseModel, ConfigDict, Field


class AnomalyInferenceRequest(BaseModel):
    equipment_id: str = Field(..., examples=["press-01"])
    sensor_values: dict[str, float] = Field(default_factory=dict)


class AnomalyInferenceResponse(BaseModel):
    model_config = ConfigDict(protected_namespaces=())

    equipment_id: str
    is_anomaly: bool
    score: float
    model_name: str


class DocumentIndexResponse(BaseModel):
    document_id: str
    status: str


class RagQueryRequest(BaseModel):
    question: str
    top_k: int = Field(default=3, ge=1, le=10)


class RagQueryResponse(BaseModel):
    answer: str
    sources: list[str] = Field(default_factory=list)
