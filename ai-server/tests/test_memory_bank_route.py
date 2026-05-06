from fastapi.testclient import TestClient

from application.exceptions import AppException
from application.models.generate_memory_bank_usecase import GenerateMemoryBankUseCase
from container.model_container import create_generate_memory_bank_usecase
from domain.models.memory_bank import ModelCategory, ModelProfile
from main import app


PNG_BYTES = (
    b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x02\x00\x00\x00\x90wS\xde\x00\x00\x00\x0cIDATx\x9cc\xf8\xff\xff?"
    b"\x00\x05\xfe\x02\xfeA\xe2^\x9b\x00\x00\x00\x00IEND\xaeB`\x82"
)


def test_runtime_openapi_contains_memory_bank_route() -> None:
    paths = app.openapi()["paths"]

    assert "/ai/v1/internal/models/memory-bank" in paths
    assert "post" in paths["/ai/v1/internal/models/memory-bank"]


def test_runtime_memory_bank_route_empty_json_is_not_404() -> None:
    client = TestClient(app)

    response = client.post("/ai/v1/internal/models/memory-bank", json={})

    assert response.status_code in (400, 422)


def test_memory_bank_route_rejects_nine_normal_images() -> None:
    client = TestClient(app)

    response = client.post("/ai/v1/internal/models/memory-bank", json=payload(9))

    assert response.status_code == 422
    assert response.json()["errorCode"] == "NORMAL_IMAGE_COUNT_TOO_SMALL"


def test_memory_bank_route_accepts_ten_images_until_normal_lookup() -> None:
    with override_usecase(FakeStorage(missing_normal=True)):
        client = TestClient(app)

        response = client.post("/ai/v1/internal/models/memory-bank", json=payload(10))

    assert response.status_code == 404
    assert response.json()["errorCode"] == "NORMAL_IMAGE_NOT_FOUND"


def test_memory_bank_route_accepts_fifty_images() -> None:
    storage = FakeStorage()
    with override_usecase(storage):
        client = TestClient(app)

        response = client.post("/ai/v1/internal/models/memory-bank", json=payload(50))

    assert response.status_code == 200
    assert response.json()["data"]["normalImageCount"] == 50


def test_memory_bank_route_reports_missing_normal_image() -> None:
    with override_usecase(FakeStorage(missing_normal=True)):
        client = TestClient(app)

        response = client.post("/ai/v1/internal/models/memory-bank", json=payload(10))

    assert response.status_code == 404
    assert response.json()["errorCode"] == "NORMAL_IMAGE_NOT_FOUND"


def test_memory_bank_route_uploads_memory_bank_on_success() -> None:
    storage = FakeStorage()
    with override_usecase(storage):
        client = TestClient(app)

        response = client.post("/ai/v1/internal/models/memory-bank", json=payload(10))

    assert response.status_code == 200
    assert response.json()["data"]["memoryBankFileKey"] == "models/generated/test/memory_bank.pt"
    assert response.json()["data"]["configFileKey"] == "models/generated/test/config.json"
    assert response.json()["data"]["ckptFileKey"] == "models/base/speed-object/model.ckpt"
    assert storage.uploaded["models/generated/test/memory_bank.pt"] == b"memory-bank"
    assert b'"modelCategory": "OBJECT"' in storage.uploaded["models/generated/test/config.json"]


def test_memory_bank_route_distinguishes_route_404() -> None:
    client = TestClient(app)

    response = client.post("/ai/v1/internal/models/missing-route", json={})

    assert response.status_code == 404
    assert response.json()["errorCode"] == "ROUTE_NOT_FOUND"


def payload(count: int) -> dict:
    return {
        "modelCategory": "OBJECT",
        "modelProfile": "SPEED",
        "ckptFileKey": "models/base/speed-object/model.ckpt",
        "configFileKey": "models/base/speed-object/config.json",
        "normalImageFileKeys": [f"models/tmp/normal-{index}.png" for index in range(count)],
        "outputPrefix": "models/generated/test",
    }


class FakeStorage:
    def __init__(self, *, missing_normal: bool = False) -> None:
        self.missing_normal = missing_normal
        self.uploaded: dict[str, bytes] = {}

    def download_model_object(self, object_key: str) -> bytes:
        return b"model"

    def download_config_object(self, object_key: str) -> bytes:
        return b'{"modelCategory":"OBJECT","modelProfile":"SPEED","imageSize":[224,224],"layers":["layer2"],"targetMemoryBankSize":10}'

    def download_ckpt_object(self, object_key: str) -> bytes:
        return b"ckpt"

    def download_normal_image_object(self, object_key: str) -> bytes:
        if self.missing_normal:
            raise AppException(404, "Normal image not found", f"MinIO object not found: {object_key}", "NORMAL_IMAGE_NOT_FOUND")
        return PNG_BYTES

    def upload_model_object(self, object_key: str, content: bytes, content_type: str) -> None:
        self.uploaded[object_key] = content


class FakeGeneratorFactory:
    def create_from_spec(self, spec):
        return FakeGenerator()

    def to_spec(self, category: ModelCategory, profile: ModelProfile, config: dict):
        from domain.models.memory_bank import MemoryBankProfileSpec

        return MemoryBankProfileSpec(
            category=category,
            profile=profile,
            pipeline="test",
            image_size=(224, 224),
            target_memory_bank_size=10,
            layers=("layer2",),
        )


class FakeGenerator:
    def generate(self, *, image_bytes_list: list[bytes], config: dict, ckpt_bytes: bytes, request_id: str) -> bytes:
        return b"memory-bank"


class override_usecase:
    def __init__(self, storage: FakeStorage) -> None:
        self.storage = storage

    def __enter__(self):
        app.dependency_overrides[create_generate_memory_bank_usecase] = lambda: GenerateMemoryBankUseCase(
            self.storage,
            FakeGeneratorFactory(),
        )

    def __exit__(self, exc_type, exc, tb):
        app.dependency_overrides.pop(create_generate_memory_bank_usecase, None)
