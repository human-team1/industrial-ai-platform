from __future__ import annotations

from io import BytesIO

import numpy as np
import pytest
from fastapi.testclient import TestClient
from PIL import Image

from container.dependencies import get_vision_inference_service
from domain.vision_models import InferenceOutput
from infrastructure.image_preprocessor import VisionImagePreprocessor
from infrastructure.quality_evaluator import QualityEvaluator
from main import app
from tests.test_vision_inference import FakeStorage, base_payload, build_service, create_image_bytes


class StubInferencer:
    def __init__(self, score: float, confidence: float) -> None:
        self.score = score
        self.confidence = confidence

    def infer(self, image, model, config, memory_bank):
        return InferenceOutput(
            score=self.score,
            confidence=self.confidence,
            decision_code="NORMAL",
            anomaly_map=np.zeros((8, 8), dtype=np.float32),
        )


def _seed_fake_storage(storage: FakeStorage) -> None:
    config_bytes = b'{"input_size":[256,256],"threshold":0.65,"model_type":"patchcore"}'
    buffer = BytesIO()
    np.save(buffer, np.random.rand(8, 32).astype(np.float32))
    storage.objects[("inspection-artifacts", "inspections/1001/original/sample.jpg")] = create_image_bytes((120, 130, 140), textured=True)
    storage.objects[("models", "models/1/versions/10/model.ckpt")] = b"fake-ckpt"
    storage.objects[("models", "models/1/versions/10/config.json")] = config_bytes
    storage.objects[("models", "models/1/versions/10/memory_bank.npy")] = buffer.getvalue()


def _make_service_with_inferencer(inferencer):
    from application.vision_inference_service import VisionInferenceService
    from config.settings import get_settings
    from infrastructure.concurrency.inference_limiter import InferenceLimiter
    from infrastructure.heatmap_generator import HeatmapGenerator
    from infrastructure.memory_bank_loader import MemoryBankLoader
    from infrastructure.vision_config_loader import VisionConfigLoader
    from infrastructure.vision_model_loader import VisionModelLoader

    settings = get_settings()
    storage = FakeStorage()
    _seed_fake_storage(storage)
    service = VisionInferenceService(
        storage=storage,
        config_loader=VisionConfigLoader(),
        model_loader=VisionModelLoader(),
        memory_bank_loader=MemoryBankLoader(),
        preprocessor=VisionImagePreprocessor(),
        quality_evaluator=QualityEvaluator(),
        inferencer=inferencer,
        heatmap_generator=HeatmapGenerator(),
        inference_limiter=InferenceLimiter(max_concurrency=1, queue_size=4),
        inspection_artifact_bucket_name=settings.minio_bucket_inspection_artifacts,
        model_bucket_name=settings.minio_bucket_models,
    )
    return service, storage


def test_no12_preprocessor_resizes_to_input_size():
    image_bytes = create_image_bytes((50, 80, 200))
    preprocessor = VisionImagePreprocessor()
    result = preprocessor.preprocess(image_bytes, roi=None, input_size="128x128", config={})
    assert result.image_array.shape == (128, 128, 3)
    assert result.image_array.dtype == np.uint8
    assert result.resized_size == (128, 128)


def test_no12_preprocessor_uses_config_input_size_when_request_missing():
    image_bytes = create_image_bytes((10, 20, 30))
    preprocessor = VisionImagePreprocessor()
    result = preprocessor.preprocess(image_bytes, roi=None, input_size=None, config={"input_size": [64, 64]})
    assert result.resized_size == (64, 64)
    assert result.image_array.shape == (64, 64, 3)


def test_no13_quality_detects_dark_image():
    image_array = np.zeros((64, 64, 3), dtype=np.uint8)
    metrics = QualityEvaluator().evaluate(image_array)
    assert metrics.status == "FAILED"
    assert metrics.reason == "TOO_DARK"


def test_no13_quality_detects_too_bright_image():
    image_array = np.full((64, 64, 3), 250, dtype=np.uint8)
    metrics = QualityEvaluator().evaluate(image_array)
    assert metrics.status == "FAILED"
    assert metrics.reason == "TOO_BRIGHT"


def test_no13_quality_detects_low_contrast():
    image_array = np.full((64, 64, 3), 128, dtype=np.uint8)
    metrics = QualityEvaluator().evaluate(image_array)
    assert metrics.status == "FAILED"
    assert metrics.reason == "LOW_CONTRAST"


def test_no14_missing_memory_bank_file_key_returns_400():
    service, _ = build_service()
    payload = base_payload()
    del payload["model"]["memoryBankFileKey"]
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 400
    app.dependency_overrides.clear()


def test_no14_missing_ckpt_file_key_returns_400():
    service, _ = build_service()
    payload = base_payload()
    del payload["model"]["ckptFileKey"]
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 400
    app.dependency_overrides.clear()


def test_no15_score_above_threshold_returns_defect():
    service, _ = _make_service_with_inferencer(StubInferencer(score=0.82, confidence=0.9))
    payload = base_payload()
    payload["threshold"]["anomalyThreshold"] = 0.75
    payload["threshold"]["lowConfidenceThreshold"] = 0.55
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 200
    body = response.json()
    assert body["data"]["decisionCode"] == "DEFECT"
    assert body["data"]["score"] == pytest.approx(0.82, abs=1e-6)
    app.dependency_overrides.clear()


def test_no15_score_below_threshold_returns_normal():
    service, _ = _make_service_with_inferencer(StubInferencer(score=0.40, confidence=0.9))
    payload = base_payload()
    payload["threshold"]["anomalyThreshold"] = 0.75
    payload["threshold"]["lowConfidenceThreshold"] = 0.55
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 200
    assert response.json()["data"]["decisionCode"] == "NORMAL"
    app.dependency_overrides.clear()


def test_no16_low_confidence_returns_recheck():
    service, _ = _make_service_with_inferencer(StubInferencer(score=0.30, confidence=0.42))
    payload = base_payload()
    payload["threshold"]["anomalyThreshold"] = 0.75
    payload["threshold"]["lowConfidenceThreshold"] = 0.55
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 200
    assert response.json()["data"]["decisionCode"] == "RECHECK"
    app.dependency_overrides.clear()
