from __future__ import annotations

from io import BytesIO

import numpy as np
from PIL import Image, UnidentifiedImageError

from application.exceptions import AppException
from domain.models.memory_bank import MemoryBankGenerator, MemoryBankProfileSpec


class PatchCoreMemoryBankGenerator(MemoryBankGenerator):
    def __init__(self, spec: MemoryBankProfileSpec) -> None:
        self._spec = spec

    def generate(self, *, image_bytes_list: list[bytes], config: dict, ckpt_bytes: bytes) -> bytes:
        try:
            features = [self._extract_features(image_bytes) for image_bytes in image_bytes_list]
            memory_bank = np.concatenate(features, axis=0).astype(np.float32)
            memory_bank = self._subsample(memory_bank, self._spec.target_memory_bank_size)
            return self._serialize(memory_bank, config)
        except AppException:
            raise
        except Exception as exc:
            raise AppException(500, "Feature extraction failed", "memory_bank feature 추출에 실패했습니다.", "MEMORY_BANK_FEATURE_FAILED") from exc

    def _extract_features(self, image_bytes: bytes) -> np.ndarray:
        try:
            with Image.open(BytesIO(image_bytes)) as image:
                image = image.convert("RGB").resize(self._spec.image_size)
                array = np.asarray(image, dtype=np.float32) / 255.0
        except (UnidentifiedImageError, OSError) as exc:
            raise AppException(422, "Invalid normal image", "손상되었거나 읽을 수 없는 정상 이미지가 포함되어 있습니다.", "INVALID_NORMAL_IMAGE") from exc

        patch_size = 16
        height, width, channels = array.shape
        patches = array.reshape(height // patch_size, patch_size, width // patch_size, patch_size, channels)
        patches = patches.transpose(0, 2, 1, 3, 4).reshape(-1, patch_size, patch_size, channels)
        mean = patches.mean(axis=(1, 2))
        std = patches.std(axis=(1, 2))
        return np.concatenate([mean, std], axis=1)

    def _subsample(self, memory_bank: np.ndarray, target_size: int) -> np.ndarray:
        if memory_bank.shape[0] <= target_size:
            return memory_bank
        indices = np.linspace(0, memory_bank.shape[0] - 1, target_size, dtype=np.int64)
        return memory_bank[indices]

    def _serialize(self, memory_bank: np.ndarray, config: dict) -> bytes:
        metadata = {
            "model_category": self._spec.category.value,
            "model_profile": self._spec.profile.value,
            "pipeline": self._spec.pipeline,
            "image_size": list(self._spec.image_size),
            "layers": list(self._spec.layers),
            "target_memory_bank_size": self._spec.target_memory_bank_size,
        }
        try:
            import torch

            buffer = BytesIO()
            torch.save({"memory_bank": memory_bank, "metadata": metadata}, buffer)
            return buffer.getvalue()
        except ImportError:
            buffer = BytesIO()
            np.savez_compressed(buffer, memory_bank=memory_bank, metadata=np.array([str(metadata)]))
            return buffer.getvalue()
