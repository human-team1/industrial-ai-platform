from __future__ import annotations

import sys
from io import BytesIO
from pathlib import Path

import pytest
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from application.exceptions import AppException
from application.models.generate_memory_bank_usecase import GenerateMemoryBankUseCase
from domain.models.memory_bank import GenerateMemoryBankCommand, ModelCategory, ModelProfile
from infrastructure.modeling.memory_bank_generator_factory import MemoryBankGeneratorFactory


class FakeModelStorage:
    def __init__(self) -> None:
        self.objects: dict[str, bytes] = {}
        self.uploaded: dict[str, bytes] = {}

    def download_model_object(self, object_key: str) -> bytes:
        if object_key not in self.objects:
            raise AppException(404, "Object not found", "not found", "OBJECT_NOT_FOUND")
        return self.objects[object_key]

    def upload_model_object(self, object_key: str, content: bytes, content_type: str) -> None:
        self.uploaded[object_key] = content


def image_bytes() -> bytes:
    buffer = BytesIO()
    Image.new("RGB", (32, 32), color=(120, 130, 140)).save(buffer, format="PNG")
    return buffer.getvalue()


def build_command(count: int) -> GenerateMemoryBankCommand:
    return GenerateMemoryBankCommand(
        model_category=ModelCategory.TEXTURE,
        model_profile=ModelProfile.PERFORMANCE,
        normal_image_file_keys=[f"models/tmp/normal/normal_{index}.png" for index in range(count)],
        config_file_key="models/base/performance-texture/config.json",
        ckpt_file_key="models/base/performance-texture/model.ckpt",
        output_prefix="models/generated/org-1001/target-10/performance-texture/job-test",
        request_id="test-request",
    )


def test_memory_bank_generation_requires_at_least_100_images() -> None:
    usecase = GenerateMemoryBankUseCase(FakeModelStorage(), MemoryBankGeneratorFactory())

    with pytest.raises(AppException) as exc:
        usecase.execute(build_command(99))

    assert exc.value.status_code == 422
    assert exc.value.code == "NORMAL_IMAGE_COUNT_TOO_SMALL"


def test_memory_bank_generation_uploads_memory_bank() -> None:
    storage = FakeModelStorage()
    command = build_command(100)
    storage.objects[command.config_file_key] = b'{"modelCategory":"TEXTURE","modelProfile":"PERFORMANCE"}'
    storage.objects[command.ckpt_file_key] = b"fixed-ckpt"
    sample = image_bytes()
    for key in command.normal_image_file_keys:
        storage.objects[key] = sample

    result = GenerateMemoryBankUseCase(storage, MemoryBankGeneratorFactory()).execute(command)

    assert result.memory_bank_file_key.endswith("/memory_bank.pt")
    assert result.normal_image_count == 100
    assert result.memory_bank_file_key in storage.uploaded
    assert storage.uploaded[result.memory_bank_file_key]
