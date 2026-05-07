from __future__ import annotations

import asyncio
import logging
from datetime import datetime
from time import perf_counter

from application.exceptions import AppException
from domain.vision_model_profile import get_vision_model_profile_spec
from domain.vision_interfaces import (
    HeatmapGeneratorPort,
    ImagePreprocessorPort,
    InferenceLimiterPort,
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
        inference_limiter: InferenceLimiterPort,
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
        self._inference_limiter = inference_limiter
        self._inspection_artifact_bucket_name = inspection_artifact_bucket_name
        self._model_bucket_name = model_bucket_name

    async def infer_image(self, request, request_id: str):
        started_at = perf_counter()
        started_at_iso = datetime.now().isoformat()
        model_request = request.model

        logger.info(
            "vision_inference_started requestId=%s inspectionId=%s modelVersionId=%s modelCategory=%s modelProfile=%s",
            request_id,
            request.inspectionId,
            model_request.modelVersionId,
            model_request.modelCategory,
            model_request.modelProfile,
        )
        try:
            async with self._inference_limiter.limit(
                request_id=request_id,
                inspection_id=request.inspectionId,
                model_version_id=model_request.modelVersionId,
            ):
                response = await asyncio.to_thread(
                    self._run_inference_pipeline,
                    request,
                    request_id,
                    started_at,
                    started_at_iso,
                )

            logger.info(
                "vision_inference_completed requestId=%s inspectionId=%s modelVersionId=%s inferenceFinishedAt=%s latencyMs=%s status=%s",
                request_id,
                request.inspectionId,
                model_request.modelVersionId,
                datetime.now().isoformat(),
                round((perf_counter() - started_at) * 1000, 2),
                "SUCCESS",
            )
            return response
        except AppException:
            raise
        except Exception as exc:
            logger.exception(
                "vision_inference_failed requestId=%s inspectionId=%s modelVersionId=%s fileKey=%s ckptFileKey=%s latencyMs=%s failedStep=%s errorCode=%s",
                request_id,
                request.inspectionId,
                model_request.modelVersionId,
                request.fileKey,
                model_request.ckptFileKey,
                round((perf_counter() - started_at) * 1000, 2),
                "ANOMALIB_PREDICT",
                "AI_INFERENCE_FAILED",
            )
            raise AppException(
                500,
                "Inference failed",
                "이미지 추론 중 오류가 발생했습니다.",
                "AI_INFERENCE_FAILED",
            ) from exc

    def _run_inference_pipeline(
        self,
        request,
        request_id: str,
        started_at: float,
        started_at_iso: str,
    ):
        model_request = request.model
        image_bytes = self._download_inspection_file(request.fileKey)
        ckpt_bytes = self._download_model_file(
            model_request.ckptFileKey,
            "AI_MODEL_CKPT_NOT_FOUND",
            "모델 ckpt 파일을 찾을 수 없습니다.",
        )
        config_bytes = self._download_model_file(
            model_request.configFileKey,
            "AI_MODEL_CONFIG_NOT_FOUND",
            "모델 config 파일을 찾을 수 없습니다.",
        )
        memory_bank_bytes = self._download_model_file(
            model_request.memoryBankFileKey,
            "AI_MEMORY_BANK_NOT_FOUND",
            "메모리뱅크 파일을 찾을 수 없습니다.",
        )

        config = self._config_loader.load(config_bytes)
        config["modelCategory"] = model_request.modelCategory
        config["modelProfile"] = model_request.modelProfile
        model = self._model_loader.load(
            model_request.modelVersionId,
            model_request.ckptFileKey,
            model_request.configFileKey,
            model_request.memoryBankFileKey,
            ckpt_bytes,
            config,
        )
        logger.info(
            "anomalib_model_loaded requestId=%s modelVersionId=%s category=%s profile=%s inputSize=%s ckpt=%s",
            request_id,
            model_request.modelVersionId,
            model_request.modelCategory,
            model_request.modelProfile,
            model_request.inputSize,
            model_request.ckptFileKey,
        )
        memory_bank = self._memory_bank_loader.load(
            model_request.memoryBankFileKey,
            memory_bank_bytes,
            config,
        )
        roi_payload = request.roi.model_dump() if request.roi else None
        image = self._preprocessor.preprocess(
            image_bytes,
            roi_payload,
            model_request.modelCategory,
            model_request.modelProfile,
        )
        quality = self._quality_evaluator.evaluate(image.image_array)

        artifacts = []
        score = None
        confidence = 0.35
        decision_code = "RECHECK"
        anomaly_map = None

        if not request.qualityGateEnabled or quality.status != "FAILED":
            inference = self._inferencer.infer(image, model, config, memory_bank)
            spec = get_vision_model_profile_spec(model_request.modelCategory, model_request.modelProfile)
            threshold = request.threshold.anomalyThreshold
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
            logger.info(
                "anomalib_prediction_completed requestId=%s modelVersionId=%s predScore=%s imageThreshold=%s decision=%s",
                request_id,
                model_request.modelVersionId,
                score,
                threshold,
                decision_code,
            )

            if anomaly_map is not None:
                heatmap_key = f"inspections/{request.inspectionId}/artifacts/heatmap.png"
                try:
                    heatmap_bytes = self._heatmap_generator.render(
                        image.original_image_array,
                        anomaly_map,
                    )
                    self._storage.put_object(
                        self._inspection_artifact_bucket_name,
                        heatmap_key,
                        heatmap_bytes,
                        "image/png",
                    )
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
                "scoreType": "ANOMALIB_PRED_SCORE",
                "scoreSource": "anomalib.pred_score",
                "imageThreshold": threshold,
                "pixelThreshold": spec.pixel_threshold if 'spec' in locals() else None,
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
                "metadata": {
                    "backend": "anomalib",
                    "anomalibVersion": "2.4.0",
                    "modelCategory": model_request.modelCategory,
                    "modelProfile": model_request.modelProfile,
                    "inputSize": model_request.inputSize or (f"{spec.input_size}x{spec.input_size}" if 'spec' in locals() else None),
                    "scoreAggregationMethod": "anomalib_default_pred_score",
                    "anomalyMapMin": float(anomaly_map.min()) if anomaly_map is not None else None,
                    "anomalyMapMax": float(anomaly_map.max()) if anomaly_map is not None else None,
                    "anomalyMapMean": float(anomaly_map.mean()) if anomaly_map is not None else None,
                    "ckptSource": "model_artifact.CKPT",
                },
                "processedAt": datetime.now(),
            },
            "message": "이미지 추론이 완료되었습니다.",
        }
        logger.info(
            "vision_inference_completed requestId=%s inspectionId=%s modelVersionId=%s fileKey=%s ckptFileKey=%s inferenceStartedAt=%s latencyMs=%s decisionCode=%s",
            request_id,
            request.inspectionId,
            model_request.modelVersionId,
            request.fileKey,
            model_request.ckptFileKey,
            started_at_iso,
            round((perf_counter() - started_at) * 1000, 2),
            decision_code,
        )
        return response

    def _download_inspection_file(self, file_key: str) -> bytes:
        try:
            return self._storage.download_object(
                self._inspection_artifact_bucket_name,
                file_key,
            )
        except AppException as exc:
            if exc.status_code == 404:
                raise AppException(
                    404,
                    "Inspection file not found",
                    "원본 이미지를 찾을 수 없습니다.",
                    "AI_FILE_NOT_FOUND",
                ) from exc
            raise

    def _download_model_file(self, file_key: str, error_code: str, detail: str) -> bytes:
        try:
            return self._storage.download_object(self._model_bucket_name, file_key)
        except AppException as exc:
            if exc.status_code == 404:
                raise AppException(404, "Model artifact not found", detail, error_code) from exc
            raise
