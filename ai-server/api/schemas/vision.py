import re
from datetime import datetime
from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator
<<<<<<< HEAD
from typing import Literal
=======

INPUT_SIZE_PATTERN = re.compile(r"^\d+(x\d+)?$")

>>>>>>> 466d72f1078d66333c2f647399668c33240aad01

class ApiSuccessResponse(BaseModel):
    success: bool = True
    data: Any
    message: str


class ThresholdRequest(BaseModel):
    anomalyThreshold: float = Field(..., ge=0, le=1)
    lowConfidenceThreshold: float = Field(..., ge=0, le=1)


class RoiRequest(BaseModel):
    roiMode: str = "FULL_FRAME"
    roiCoordinateType: str = "NORMALIZED"
    roiX: float | None = None
    roiY: float | None = None
    roiWidth: float | None = None
    roiHeight: float | None = None

    @model_validator(mode="after")
    def validate_roi(self) -> "RoiRequest":
        if self.roiMode == "FIXED":
            fields = [self.roiX, self.roiY, self.roiWidth, self.roiHeight]
            if any(value is None for value in fields):
                raise ValueError("FIXED ROI requires roiX, roiY, roiWidth, roiHeight")
            if any(value < 0 or value > 1 for value in fields if value is not None):
                raise ValueError("ROI coordinates must be within 0~1")
            if self.roiWidth is not None and self.roiWidth <= 0:
                raise ValueError("roiWidth must be greater than 0")
            if self.roiHeight is not None and self.roiHeight <= 0:
                raise ValueError("roiHeight must be greater than 0")
            if self.roiX is not None and self.roiWidth is not None and self.roiX + self.roiWidth > 1:
                raise ValueError("ROI x range must stay within 0~1")
            if self.roiY is not None and self.roiHeight is not None and self.roiY + self.roiHeight > 1:
                raise ValueError("ROI y range must stay within 0~1")
        return self


class VisionModelRequest(BaseModel):
    modelVersionId: int
    modelCategory: str
    modelProfile: str
    framework: str | None = None
    inputSize: str | None = None
    ckptFileKey: str
    configFileKey: str
    memoryBankFileKey: str
    labelsFileKey: str | None = None

    @field_validator("modelCategory")
    @classmethod
    def validate_category(cls, value: str) -> str:
        if value not in {"OBJECT", "TEXTURE"}:
            raise ValueError("modelCategory must be OBJECT or TEXTURE")
        return value

    @field_validator("modelProfile")
    @classmethod
    def validate_profile(cls, value: str) -> str:
        if value not in {"SPEED", "PERFORMANCE"}:
            raise ValueError("modelProfile must be SPEED or PERFORMANCE")
        return value

    @field_validator("ckptFileKey", "configFileKey", "memoryBankFileKey")
    @classmethod
    def validate_required_file_key(cls, value: str) -> str:
        if not value or not value.strip():
            raise ValueError("file key must not be blank")
        return value.strip()

    @field_validator("inputSize")
    @classmethod
    def validate_input_size(cls, value: str | None) -> str | None:
        if value is None:
            return None
        normalized = value.strip()
        if normalized == "":
            return None
        if not INPUT_SIZE_PATTERN.match(normalized):
            raise ValueError("inputSize must be 'W' or 'WxH' format (e.g. '224' or '224x224')")
        return normalized


class InferImageRequest(BaseModel):
    inspectionId: int
    fileKey: str
    targetId: int | None = None
    model: VisionModelRequest
    roi: RoiRequest | None = None
    qualityGateEnabled: bool = True
    threshold: ThresholdRequest

    @field_validator("fileKey")
    @classmethod
    def validate_file_key(cls, value: str) -> str:
        if not value or not value.strip():
            raise ValueError("fileKey must not be blank")
        return value.strip()


class QualityResponse(BaseModel):
    status: str
    reason: str | None = None
    brightness: float | None = None
    contrast: float | None = None
    blurScore: float | None = None
    saturation: float | None = None


class ArtifactResponse(BaseModel):
    artifactType: str
    fileKey: str


class InferImageResponseData(BaseModel):
    inspectionId: int
    modelVersionId: int
    score: float | None = None
    confidence: float
    # Deprecated: Spring DecisionCalculator is the source of truth.
    # FastAPI returns its internal hint here for diagnostics/back-compat only;
    # Spring ignores this field when computing the final decisionCode.
    decisionCode: str | None = None
    quality: QualityResponse
    artifacts: list[ArtifactResponse] = Field(default_factory=list)
    regions: list[Any] = Field(default_factory=list)
    processedAt: datetime


class InferImageApiResponse(ApiSuccessResponse):
    model_config = ConfigDict(protected_namespaces=())

    data: InferImageResponseData

class MemoryBankRequest(BaseModel):
    modelCategory: Literal["OBJECT", "TEXTURE"]
    modelProfile: Literal["SPEED", "PERFORMANCE"]
    normalImageFileKeys: list[str] = Field(..., min_length=100)
    configFileKey: str
    ckptFileKey: str
    outputPrefix: str

    @field_validator("normalImageFileKeys")
    @classmethod
    def validate_normal_image_file_keys(cls, value: list[str]) -> list[str]:
        cleaned = [v.strip() for v in value if v and v.strip()]

        if len(cleaned) < 100:
            raise ValueError("normalImageFileKeys must contain at least 100 images")

        return cleaned

    @field_validator("ckptFileKey", "configFileKey", "outputPrefix")
    @classmethod
    def validate_required_key(cls, value: str) -> str:
        if not value or not value.strip():
            raise ValueError("ckptFileKey, configFileKey, outputPrefix must not be blank")
        return value.strip()


class MemoryBankResponseData(BaseModel):
    memoryBankFileKey: str
    normalImageCount: int
    modelCategory: str
    modelProfile: str
    createdAt: datetime


class MemoryBankApiResponse(ApiSuccessResponse):
    data: MemoryBankResponseData