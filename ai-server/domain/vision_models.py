from dataclasses import dataclass, field
from datetime import datetime
from typing import Any

import numpy as np


@dataclass(slots=True)
class LoadedVisionModel:
    model_version_id: int
    ckpt_file_key: str
    config_file_key: str
    memory_bank_file_key: str
    ckpt_bytes: bytes
    config: dict[str, Any]
    loaded_at: datetime
    runtime_model: Any = None
    runtime_metadata: dict[str, Any] = field(default_factory=dict)


@dataclass(slots=True)
class LoadedMemoryBank:
    source_type: str
    payload: Any
    shape: tuple[int, ...] | None = None
    metadata: dict[str, Any] = field(default_factory=dict)


@dataclass(slots=True)
class PreprocessedImage:
    image_array: np.ndarray
    original_image_array: np.ndarray
    original_size: tuple[int, int]
    resized_size: tuple[int, int]


@dataclass(slots=True)
class QualityMetrics:
    status: str
    reason: str | None
    brightness: float | None
    contrast: float | None
    blur_score: float | None
    saturation: float | None


@dataclass(slots=True)
class InferenceOutput:
    score: float | None
    confidence: float
    decision_code: str
    anomaly_map: np.ndarray | None
    score_type: str = "ANOMALIB_PRED_SCORE"
    score_source: str = "anomalib.pred_score"
    image_threshold: float | None = None
    pixel_threshold: float | None = None
