from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from typing import Protocol


class ModelCategory(str, Enum):
    OBJECT = "OBJECT"
    TEXTURE = "TEXTURE"


class ModelProfile(str, Enum):
    SPEED = "SPEED"
    PERFORMANCE = "PERFORMANCE"


@dataclass(frozen=True)
class MemoryBankProfileSpec:
    category: ModelCategory
    profile: ModelProfile
    pipeline: str
    image_size: tuple[int, int]
    target_memory_bank_size: int
    layers: tuple[str, ...]


@dataclass(frozen=True)
class GenerateMemoryBankCommand:
    model_category: ModelCategory
    model_profile: ModelProfile
    normal_image_file_keys: list[str]
    config_file_key: str
    ckpt_file_key: str
    output_prefix: str
    request_id: str


@dataclass(frozen=True)
class GenerateMemoryBankResult:
    memory_bank_file_key: str
    normal_image_count: int
    model_category: ModelCategory
    model_profile: ModelProfile
    created_at: str


class ModelStoragePort(Protocol):
    def download_model_object(self, object_key: str) -> bytes: ...

    def upload_model_object(self, object_key: str, content: bytes, content_type: str) -> None: ...


class MemoryBankGenerator(Protocol):
    def generate(self, *, image_bytes_list: list[bytes], config: dict, ckpt_bytes: bytes) -> bytes: ...
