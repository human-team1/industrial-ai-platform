from __future__ import annotations

import logging

import numpy as np
import torch

from application.exceptions import AppException
from domain.vision_models import InferenceOutput, LoadedMemoryBank, LoadedVisionModel, PreprocessedImage

logger = logging.getLogger(__name__)


class AnomalibPatchcoreInferencer:
    def infer(
        self,
        image: PreprocessedImage,
        model: LoadedVisionModel,
        config: dict,
        memory_bank: LoadedMemoryBank,
    ) -> InferenceOutput:
        del memory_bank  # memory bank is embedded in anomalib checkpoint.
        if model.runtime_model is None:
            raise AppException(500, "Anomalib model not loaded", "Anomalib 모델이 로드되지 않았습니다.", "ANOMALIB_MODEL_NOT_LOADED")

        try:
            runtime_model = model.runtime_model
            device = model.runtime_metadata.get("device", "cpu")
            tensor = torch.from_numpy(image.image_array.transpose(2, 0, 1)).float().unsqueeze(0).to(device)
            runtime_model.eval()
            with torch.no_grad():
                prediction = runtime_model.model(tensor)
            pred_score = float(prediction.pred_score.flatten()[0].detach().cpu().item())
            anomaly_map = prediction.anomaly_map[0, 0].detach().cpu().numpy().astype(np.float32)
            confidence = float(np.clip(pred_score / max(pred_score + 1.0, 1.0), 0.0, 1.0))
            return InferenceOutput(
                score=pred_score,
                confidence=confidence,
                decision_code="NORMAL",
                anomaly_map=anomaly_map,
            )
        except AppException:
            raise
        except Exception as exc:
            logger.exception("anomalib_prediction_failed modelVersionId=%s", model.model_version_id)
            raise AppException(500, "Anomalib prediction failed", "Anomalib 추론 중 오류가 발생했습니다.", "ANOMALIB_PREDICT_FAILED") from exc
