from __future__ import annotations

from io import BytesIO

import numpy as np
import pytest
from fastapi.testclient import TestClient
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


# No.14: 내부 추론 API(/ai/v1/internal/vision/infer-image) 기준 검증 정책.
# - 필수 필드 누락(memoryBankFileKey/ckptFileKey/configFileKey 등)은 Pydantic ValidationError 로 보고 422 통일.
# - JSON 본체 자체가 깨진 경우(json_invalid)만 400 으로 분리(handle_validation_exception 주석 참고).
# 외부 공개 API의 "필수 파라미터 자체 누락 = 400" 정책과는 분리되어 있음.
def test_no14_missing_memory_bank_file_key_returns_422():
    """[내부 추론 API] memoryBankFileKey 누락 -> 422 (Pydantic missing)."""
    service, _ = build_service()
    payload = base_payload()
    del payload["model"]["memoryBankFileKey"]
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 422
    app.dependency_overrides.clear()


def test_no14_missing_ckpt_file_key_returns_422():
    """[내부 추론 API] ckptFileKey 누락 -> 422 (Pydantic missing)."""
    service, _ = build_service()
    payload = base_payload()
    del payload["model"]["ckptFileKey"]
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 422
    app.dependency_overrides.clear()


def test_no14_missing_config_file_key_returns_422():
    """[내부 추론 API] configFileKey 누락 -> 422 (Pydantic missing)."""
    service, _ = build_service()
    payload = base_payload()
    del payload["model"]["configFileKey"]
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 422
    app.dependency_overrides.clear()


def test_no14_blank_memory_bank_file_key_returns_422():
    """[내부 추론 API] memoryBankFileKey 공백 -> 422 (field_validator 형식 위반)."""
    service, _ = build_service()
    payload = base_payload()
    payload["model"]["memoryBankFileKey"] = "   "
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 422
    app.dependency_overrides.clear()


def test_no14_json_invalid_still_returns_400():
    """[내부 추론 API] 요청 JSON 본체 자체가 깨진 경우(json_invalid)는 422가 아닌 400 유지."""
    service, _ = build_service()
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post(
        "/ai/v1/internal/vision/infer-image",
        data="{invalid json",
        headers={"Content-Type": "application/json"},
    )
    assert response.status_code == 400
    app.dependency_overrides.clear()


def test_no12_invalid_input_size_returns_422():
    service, _ = build_service()
    payload = base_payload()
    payload["model"]["inputSize"] = "abc"
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 422
    app.dependency_overrides.clear()


def test_no12_input_size_with_asterisk_returns_422():
    service, _ = build_service()
    payload = base_payload()
    payload["model"]["inputSize"] = "224*224"
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 422
    app.dependency_overrides.clear()


def test_no12_input_size_single_dimension_passes():
    service, _ = build_service()
    payload = base_payload()
    payload["model"]["inputSize"] = "224"
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code != 422
    app.dependency_overrides.clear()


def test_no12_input_size_wxh_passes():
    service, _ = build_service()
    payload = base_payload()
    payload["model"]["inputSize"] = "224x224"
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code != 422
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
