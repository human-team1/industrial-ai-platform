from __future__ import annotations

import logging
from datetime import datetime
from time import perf_counter

from application.exceptions import AppException
from domain.vision_interfaces import (
    HeatmapGeneratorPort,
    ImagePreprocessorPort,
    MemoryBankLoaderPort,
    QualityEvaluatorPort,
    StoragePort,
    VisionConfigLoaderPort,
    VisionInferencerPort,
    VisionModelLoaderPort,
)

logger = logging.getLogger(__name__)


class VisionInferenceService:
    def __init__(
        self,
        storage: StoragePort,
        config_loader: VisionConfigLoaderPort,
        model_loader: VisionModelLoaderPort,
        memory_bank_loader: MemoryBankLoaderPort,
        preprocessor: ImagePreprocessorPort,
        quality_evaluator: QualityEvaluatorPort,
        inferencer: VisionInferencerPort,
        heatmap_generator: HeatmapGeneratorPort,
        inspection_artifact_bucket_name: str,
        model_bucket_name: str,
    ) -> None:
        self._storage = storage
        self._config_loader = config_loader
        self._model_loader = model_loader
        self._memory_bank_loader = memory_bank_loader
        self._preprocessor = preprocessor
        self._quality_evaluator = quality_evaluator
        self._inferencer = inferencer
        self._heatmap_generator = heatmap_generator
        self._inspection_artifact_bucket_name = inspection_artifact_bucket_name
        self._model_bucket_name = model_bucket_name

    def infer_image(self, request, request_id: str):
        started_at = perf_counter()
        model_request = request.model

        try:
            image_bytes = self._download_inspection_file(request.fileKey)
            ckpt_bytes = self._download_model_file(model_request.ckptFileKey, "AI_MODEL_CKPT_NOT_FOUND", "모델 ckpt 파일을 찾을 수 없습니다.")
            config_bytes = self._download_model_file(model_request.configFileKey, "AI_MODEL_CONFIG_NOT_FOUND", "모델 config 파일을 찾을 수 없습니다.")
            memory_bank_bytes = self._download_model_file(
                model_request.memoryBankFileKey,
                "AI_MEMORY_BANK_NOT_FOUND",
                "메모리뱅크 파일을 찾을 수 없습니다.",
            )

            config = self._config_loader.load(config_bytes)
            model = self._model_loader.load(
                model_request.modelVersionId,
                model_request.ckptFileKey,
                model_request.configFileKey,
                model_request.memoryBankFileKey,
                ckpt_bytes,
                config,
            )
            memory_bank = self._memory_bank_loader.load(model_request.memoryBankFileKey, memory_bank_bytes, config)
            roi_payload = request.roi.model_dump() if request.roi else None
            image = self._preprocessor.preprocess(image_bytes, roi_payload, model_request.inputSize, config)
            quality = self._quality_evaluator.evaluate(image.image_array)

            artifacts = []
            score = None
            confidence = 0.35
            decision_code = "RECHECK"
            anomaly_map = None

            if not request.qualityGateEnabled or quality.status != "FAILED":
                inference = self._inferencer.infer(image, model, config, memory_bank)
                threshold = request.threshold.anomalyThreshold or config.get("anomalyThreshold") or config.get("threshold") or config.get("thresholdDefault") or 0.75
                low_confidence_threshold = request.threshold.lowConfidenceThreshold
                score = inference.score
                confidence = inference.confidence
                anomaly_map = inference.anomaly_map
                if quality.status == "FAILED":
                    decision_code = "RECHECK"
                elif score is not None and score >= threshold:
                    decision_code = "DEFECT"
                elif confidence < low_confidence_threshold:
                    decision_code = "RECHECK"
                else:
                    decision_code = "NORMAL"

                if anomaly_map is not None:
                    heatmap_key = f"inspections/{request.inspectionId}/artifacts/heatmap.png"
                    try:
                        heatmap_bytes = self._heatmap_generator.render(image.image_array, anomaly_map)
                        self._storage.put_object(self._inspection_artifact_bucket_name, heatmap_key, heatmap_bytes, "image/png")
                        artifacts.append({"artifactType": "HEATMAP", "fileKey": heatmap_key})
                    except Exception:
                        logger.warning(
                            "heatmap upload skipped requestId=%s inspectionId=%s modelVersionId=%s fileKey=%s",
                            request_id,
                            request.inspectionId,
                            model_request.modelVersionId,
                            request.fileKey,
                            exc_info=True,
                        )

            response = {
                "success": True,
                "data": {
                    "inspectionId": request.inspectionId,
                    "modelVersionId": model_request.modelVersionId,
                    "score": score,
                    "confidence": confidence,
                    "decisionCode": decision_code,
                    "quality": {
                        "status": quality.status,
                        "reason": quality.reason,
                        "brightness": quality.brightness,
                        "contrast": quality.contrast,
                        "blurScore": quality.blur_score,
                        "saturation": quality.saturation,
                    },
                    "artifacts": artifacts,
                    "regions": [],
                    "processedAt": datetime.now(),
                },
                "message": "이미지 추론이 완료되었습니다.",
            }
            logger.info(
                "vision infer completed requestId=%s inspectionId=%s modelVersionId=%s fileKey=%s ckptFileKey=%s durationMs=%s decisionCode=%s",
                request_id,
                request.inspectionId,
                model_request.modelVersionId,
                request.fileKey,
                model_request.ckptFileKey,
                round((perf_counter() - started_at) * 1000, 2),
                decision_code,
            )
            return response
        except AppException:
            raise
        except Exception as exc:
            logger.exception(
                "vision infer failed requestId=%s inspectionId=%s modelVersionId=%s fileKey=%s ckptFileKey=%s errorCode=%s",
                request_id,
                request.inspectionId,
                model_request.modelVersionId,
                request.fileKey,
                model_request.ckptFileKey,
                "AI_INFERENCE_FAILED",
            )
            raise AppException(500, "Inference failed", "이미지 추론 중 오류가 발생했습니다.", "AI_INFERENCE_FAILED") from exc

    def _download_inspection_file(self, file_key: str) -> bytes:
        try:
            return self._storage.download_object(self._inspection_artifact_bucket_name, file_key)
        except AppException as exc:
            if exc.status_code == 404:
                raise AppException(404, "Inspection file not found", "원본 이미지를 찾을 수 없습니다.", "AI_FILE_NOT_FOUND") from exc
            raise

    def _download_model_file(self, file_key: str, error_code: str, detail: str) -> bytes:
        try:
            return self._storage.download_object(self._model_bucket_name, file_key)
        except AppException as exc:
            if exc.status_code == 404:
                raise AppException(404, "Model artifact not found", detail, error_code) from exc
            raise
