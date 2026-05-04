import asyncio
from io import BytesIO
from threading import Lock
from time import sleep

import httpx
import numpy as np
import pytest
from fastapi.testclient import TestClient
from PIL import Image

from container.dependencies import get_vision_inference_service
from domain.vision_models import InferenceOutput
from main import app


class FakeStorage:
    def __init__(self) -> None:
        self.objects: dict[tuple[str, str], bytes] = {}

    def download_object(self, bucket_name: str, object_name: str) -> bytes:
        key = (bucket_name, object_name)
        if key not in self.objects:
            from application.exceptions import AppException

            raise AppException(404, "Object not found", "Object not found", "AI_OBJECT_NOT_FOUND")
        return self.objects[key]

    def put_object(self, bucket_name: str, object_name: str, content: bytes, content_type: str = "application/octet-stream") -> None:
        self.objects[(bucket_name, object_name)] = content


def build_service():
    from application.vision_inference_service import VisionInferenceService
    from config.settings import get_settings
    from infrastructure.concurrency.inference_limiter import InferenceLimiter
    from infrastructure.fallback_inferencer import StatisticalFallbackInferencer
    from infrastructure.heatmap_generator import HeatmapGenerator
    from infrastructure.image_preprocessor import VisionImagePreprocessor
    from infrastructure.memory_bank_loader import MemoryBankLoader
    from infrastructure.quality_evaluator import QualityEvaluator
    from infrastructure.vision_config_loader import VisionConfigLoader
    from infrastructure.vision_model_loader import VisionModelLoader

    settings = get_settings()
    storage = FakeStorage()
    service = VisionInferenceService(
        storage=storage,
        config_loader=VisionConfigLoader(),
        model_loader=VisionModelLoader(),
        memory_bank_loader=MemoryBankLoader(),
        preprocessor=VisionImagePreprocessor(),
        quality_evaluator=QualityEvaluator(),
        inferencer=StatisticalFallbackInferencer(),
        heatmap_generator=HeatmapGenerator(),
        inference_limiter=InferenceLimiter(
            max_concurrency=settings.max_inference_concurrency,
            queue_size=settings.inference_queue_size,
        ),
        inspection_artifact_bucket_name=settings.minio_bucket_inspection_artifacts,
        model_bucket_name=settings.minio_bucket_models,
    )
    return service, storage


class SlowTrackingInferencer:
    def __init__(self) -> None:
        self._lock = Lock()
        self.current_concurrency = 0
        self.max_concurrency = 0

    def infer(self, image, model, config, memory_bank) -> InferenceOutput:
        with self._lock:
            self.current_concurrency += 1
            self.max_concurrency = max(self.max_concurrency, self.current_concurrency)

        try:
            sleep(0.2)
            return InferenceOutput(
                score=0.9,
                confidence=0.9,
                decision_code="DEFECT",
                anomaly_map=np.zeros((8, 8), dtype=np.float32),
            )
        finally:
            with self._lock:
                self.current_concurrency -= 1


def create_image_bytes(color: tuple[int, int, int], textured: bool = False) -> bytes:
    if textured:
        image_array = np.zeros((64, 64, 3), dtype=np.uint8)
        checker = ((np.indices((64, 64)).sum(axis=0) % 2) * 255).astype(np.uint8)
        image_array[:, :, 0] = checker
        image_array[:, :, 1] = np.rot90(checker)
        image_array[:, :, 2] = color[2]
        image = Image.fromarray(image_array, "RGB")
    else:
        image = Image.new("RGB", (64, 64), color)
    buffer = BytesIO()
    image.save(buffer, format="JPEG")
    return buffer.getvalue()


def base_payload():
    return {
        "inspectionId": 1001,
        "fileKey": "inspections/1001/original/sample.jpg",
        "targetId": 1,
        "model": {
            "modelVersionId": 10,
            "modelCategory": "TEXTURE",
            "modelProfile": "PERFORMANCE",
            "framework": "PYTORCH",
            "inputSize": "256x256",
            "ckptFileKey": "models/1/versions/10/model.ckpt",
            "configFileKey": "models/1/versions/10/config.json",
            "memoryBankFileKey": "models/1/versions/10/memory_bank.npy",
            "labelsFileKey": None,
        },
        "roi": {
            "roiMode": "FULL_FRAME",
            "roiCoordinateType": "NORMALIZED",
            "roiX": None,
            "roiY": None,
            "roiWidth": None,
            "roiHeight": None,
        },
        "qualityGateEnabled": True,
        "threshold": {
            "anomalyThreshold": 0.75,
            "lowConfidenceThreshold": 0.55,
        },
    }


def test_infer_image_success():
    service, storage = build_service()
    image_bytes = create_image_bytes((120, 130, 140), textured=True)
    config_bytes = b'{"input_size":[256,256],"threshold":0.65,"model_type":"patchcore"}'
    memory_bank = np.random.rand(8, 32).astype(np.float32)
    buffer = BytesIO()
    np.save(buffer, memory_bank)

    storage.objects[("inspection-artifacts", "inspections/1001/original/sample.jpg")] = image_bytes
    storage.objects[("models", "models/1/versions/10/model.ckpt")] = b"fake-ckpt"
    storage.objects[("models", "models/1/versions/10/config.json")] = config_bytes
    storage.objects[("models", "models/1/versions/10/memory_bank.npy")] = buffer.getvalue()

    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post(
        "/ai/v1/internal/vision/infer-image",
        headers={"X-Request-Id": "pytest-vision-001"},
        json=base_payload(),
    )

    assert response.status_code == 200
    body = response.json()
    assert body["success"] is True
    assert body["data"]["modelVersionId"] == 10
    assert 0 <= body["data"]["confidence"] <= 1
    assert body["data"]["decisionCode"] in {"NORMAL", "DEFECT", "RECHECK"}
    assert ("inspection-artifacts", "inspections/1001/artifacts/heatmap.png") in storage.objects


def test_missing_memory_bank_file_key_returns_422():
    service, _ = build_service()
    payload = base_payload()
    del payload["model"]["memoryBankFileKey"]
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 422


def test_invalid_threshold_returns_422():
    service, _ = build_service()
    payload = base_payload()
    payload["threshold"]["anomalyThreshold"] = 1.5
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=payload)
    assert response.status_code == 422


def test_missing_memory_bank_object_returns_404():
    service, storage = build_service()
    storage.objects[("inspection-artifacts", "inspections/1001/original/sample.jpg")] = create_image_bytes((120, 130, 140))
    storage.objects[("models", "models/1/versions/10/model.ckpt")] = b"fake-ckpt"
    storage.objects[("models", "models/1/versions/10/config.json")] = b'{"input_size":[256,256],"threshold":0.65}'
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=base_payload())
    assert response.status_code == 404
    assert response.json()["errorCode"] == "AI_MEMORY_BANK_NOT_FOUND"


def test_quality_failure_returns_recheck():
    service, storage = build_service()
    config_bytes = b'{"input_size":[256,256],"threshold":0.65,"model_type":"patchcore"}'
    buffer = BytesIO()
    np.save(buffer, np.random.rand(4, 16).astype(np.float32))

    storage.objects[("inspection-artifacts", "inspections/1001/original/sample.jpg")] = create_image_bytes((0, 0, 0))
    storage.objects[("models", "models/1/versions/10/model.ckpt")] = b"fake-ckpt"
    storage.objects[("models", "models/1/versions/10/config.json")] = config_bytes
    storage.objects[("models", "models/1/versions/10/memory_bank.npy")] = buffer.getvalue()

    app.dependency_overrides[get_vision_inference_service] = lambda: service
    client = TestClient(app)
    response = client.post("/ai/v1/internal/vision/infer-image", json=base_payload())
    assert response.status_code == 200
    assert response.json()["data"]["decisionCode"] == "RECHECK"
    assert response.json()["data"]["quality"]["status"] == "FAILED"


@pytest.mark.anyio
async def test_inference_requests_run_one_at_a_time():
    from application.vision_inference_service import VisionInferenceService
    from config.settings import get_settings
    from infrastructure.concurrency.inference_limiter import InferenceLimiter
    from infrastructure.heatmap_generator import HeatmapGenerator
    from infrastructure.image_preprocessor import VisionImagePreprocessor
    from infrastructure.memory_bank_loader import MemoryBankLoader
    from infrastructure.quality_evaluator import QualityEvaluator
    from infrastructure.vision_config_loader import VisionConfigLoader
    from infrastructure.vision_model_loader import VisionModelLoader

    settings = get_settings()
    storage = FakeStorage()
    inferencer = SlowTrackingInferencer()
    config_bytes = b'{"input_size":[256,256],"threshold":0.65,"model_type":"patchcore"}'
    buffer = BytesIO()
    np.save(buffer, np.random.rand(8, 32).astype(np.float32))

    storage.objects[("inspection-artifacts", "inspections/1001/original/sample.jpg")] = create_image_bytes((120, 130, 140), textured=True)
    storage.objects[("models", "models/1/versions/10/model.ckpt")] = b"fake-ckpt"
    storage.objects[("models", "models/1/versions/10/config.json")] = config_bytes
    storage.objects[("models", "models/1/versions/10/memory_bank.npy")] = buffer.getvalue()

    service = VisionInferenceService(
        storage=storage,
        config_loader=VisionConfigLoader(),
        model_loader=VisionModelLoader(),
        memory_bank_loader=MemoryBankLoader(),
        preprocessor=VisionImagePreprocessor(),
        quality_evaluator=QualityEvaluator(),
        inferencer=inferencer,
        heatmap_generator=HeatmapGenerator(),
        inference_limiter=InferenceLimiter(max_concurrency=1, queue_size=5),
        inspection_artifact_bucket_name=settings.minio_bucket_inspection_artifacts,
        model_bucket_name=settings.minio_bucket_models,
    )
    app.dependency_overrides[get_vision_inference_service] = lambda: service
    transport = httpx.ASGITransport(app=app)
    async with httpx.AsyncClient(transport=transport, base_url="http://testserver") as client:
        responses = await asyncio.gather(
            *[
                client.post(
                    "/ai/v1/internal/vision/infer-image",
                    headers={"X-Request-Id": f"pytest-concurrent-{index}"},
                    json=base_payload(),
                )
                for index in range(3)
            ]
        )

    assert len(responses) == 3
    assert all(response.status_code == 200 for response in responses)
    assert inferencer.max_concurrency == 1
