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
        # memory bank는 VisionModelLoader.load() 시점에 모델에 주입됨.
        # 인퍼런서는 이미 주입된 model.runtime_model.model.memory_bank를 사용한다.
        _ = memory_bank
        if model.runtime_model is None:
            raise AppException(500, "Anomalib model not loaded", "Anomalib 모델이 로드되지 않았습니다.", "ANOMALIB_MODEL_NOT_LOADED")

        try:
            runtime_model = model.runtime_model
            device = model.runtime_metadata.get("device", "cpu")

            # inference_array: CHW float32 normalized (C-contiguous). fallback은 image_array(uint8 HWC).
            if image.inference_array is not None:
                arr_chw = image.inference_array  # 이미 CHW, contiguous
            else:
                # fallback: uint8 HWC → CHW float32 (비정규화 경로이므로 정상 추론 보장 안 됨)
                arr_chw = np.ascontiguousarray(
                    image.image_array.transpose(2, 0, 1), dtype=np.float32
                )

            logger.info(
                "inference_preprocessing_info modelVersionId=%s "
                "originalSize=%sx%s modelInputSize=%sx%s "
                "tensorShape=CHW%s dtype=%s min=%.4f max=%.4f "
                "colorMode=RGB normalize=imagenet mean=[0.485,0.456,0.406] std=[0.229,0.224,0.225]",
                model.model_version_id,
                image.original_size[0], image.original_size[1],
                image.resized_size[0], image.resized_size[1],
                arr_chw.shape,
                arr_chw.dtype,
                float(arr_chw.min()),
                float(arr_chw.max()),
            )

            tensor = torch.from_numpy(arr_chw).unsqueeze(0).to(device)
            patchcore_model = runtime_model.model
            mb = getattr(patchcore_model, "memory_bank", None)
            if mb is not None and hasattr(mb, "shape"):
                logger.debug(
                    "anomalib_memory_bank_at_inference modelVersionId=%s memoryBankShape=%s device=%s",
                    model.model_version_id,
                    tuple(mb.shape),
                    device,
                )
            runtime_model.eval()
            with torch.no_grad():
                prediction = runtime_model.model(tensor)
            pred_score = float(prediction.pred_score.flatten()[0].detach().cpu().item())
            anomaly_map = prediction.anomaly_map[0, 0].detach().cpu().numpy().astype(np.float32)
            confidence = float(np.clip(pred_score / max(pred_score + 1.0, 1.0), 0.0, 1.0))
            logger.info(
                "anomalib_prediction_completed modelVersionId=%s predScore=%.4f confidence=%.4f",
                model.model_version_id,
                pred_score,
                confidence,
            )
            return InferenceOutput(
                score=pred_score,
                confidence=confidence,
                decision_code="NORMAL",
                anomaly_map=anomaly_map,
            )
        except AppException:
            raise
        except Exception as exc:
            logger.error(
                "anomalib_prediction_failed modelVersionId=%s exceptionType=%s exceptionMessage=%s",
                model.model_version_id,
                type(exc).__name__,
                str(exc)[:300],
                exc_info=True,
            )
            raise AppException(500, "Anomalib prediction failed", "Anomalib 추론 중 오류가 발생했습니다.", "ANOMALIB_PREDICT_FAILED") from exc
