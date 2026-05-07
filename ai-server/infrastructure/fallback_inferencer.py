from __future__ import annotations

import cv2
import numpy as np

from domain.vision_model_profile import get_vision_model_profile_spec
from domain.vision_models import InferenceOutput, LoadedMemoryBank, LoadedVisionModel, PreprocessedImage


class StatisticalFallbackInferencer:
    """실제 PatchCore adapter 연결 전까지 사용하는 임시 추론기."""

    def infer(
        self,
        image: PreprocessedImage,
        model: LoadedVisionModel,
        config: dict,
        memory_bank: LoadedMemoryBank,
    ) -> InferenceOutput:
        spec = get_vision_model_profile_spec(
            str(config.get("modelCategory", "")),
            str(config.get("modelProfile", "")),
        )
        gray = cv2.cvtColor(image.image_array, cv2.COLOR_RGB2GRAY)
        normalized = gray.astype(np.float32) / 255.0
        edges = cv2.Canny(gray, 40, 120).astype(np.float32) / 255.0
        anomaly_map = cv2.GaussianBlur(np.abs(normalized - normalized.mean()) + edges, (0, 0), 1.2)

        # Fallback inferencer score uses the same scale as image_threshold.
        raw_ratio = float(np.clip(anomaly_map.mean(), 0.0, 1.0))
        score = float(raw_ratio * spec.image_threshold * 1.2)
        memory_hint = 0.0
        if memory_bank.shape:
            memory_hint = min(memory_bank.shape[-1] / 4096.0, 0.15)
        confidence = float(np.clip(0.65 + normalized.std() * 0.45 + memory_hint, 0.0, 1.0))
        return InferenceOutput(score=score, confidence=confidence, decision_code="NORMAL", anomaly_map=anomaly_map)
