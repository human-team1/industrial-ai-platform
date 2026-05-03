from __future__ import annotations

from io import BytesIO
from pathlib import PurePosixPath
from typing import Any

import numpy as np

from application.exceptions import AppException
from domain.vision_models import LoadedMemoryBank

try:
    import torch
except ImportError:  # pragma: no cover
    torch = None


class MemoryBankLoader:
    _DICT_KEYS = ("memory_bank", "embeddings", "features", "patch_features")

    def load(self, file_key: str, memory_bank_bytes: bytes, config: dict) -> LoadedMemoryBank:
        suffix = PurePosixPath(file_key).suffix.lower()

        try:
            if suffix in {".npy", ".npz"}:
                return self._load_numpy(file_key, memory_bank_bytes, suffix)
            if suffix in {".pt", ".pth"}:
                return self._load_torch(file_key, memory_bank_bytes)
        except AppException:
            raise
        except Exception as exc:
            raise AppException(500, "Memory bank load failed", "메모리뱅크 로드 중 오류가 발생했습니다.", "AI_MEMORY_BANK_LOAD_FAILED") from exc

        raise AppException(
            500,
            "Memory bank format unsupported",
            "지원하지 않는 메모리뱅크 파일 형식입니다.",
            "AI_MEMORY_BANK_FORMAT_UNSUPPORTED",
        )

    def _load_numpy(self, file_key: str, memory_bank_bytes: bytes, suffix: str) -> LoadedMemoryBank:
        buffer = BytesIO(memory_bank_bytes)
        if suffix == ".npy":
            array = np.load(buffer, allow_pickle=False)
        else:
            archive = np.load(buffer, allow_pickle=False)
            keys = list(archive.files)
            if not keys:
                raise AppException(500, "Memory bank load failed", "메모리뱅크 데이터가 비어 있습니다.", "AI_MEMORY_BANK_LOAD_FAILED")
            array = archive[keys[0]]
        return LoadedMemoryBank(source_type="numpy", payload=array, shape=tuple(array.shape))

    def _load_torch(self, file_key: str, memory_bank_bytes: bytes) -> LoadedMemoryBank:
        if torch is None:
            raise AppException(
                500,
                "Memory bank load failed",
                "torch가 설치되어 있지 않아 .pt/.pth 메모리뱅크를 로드할 수 없습니다.",
                "AI_MEMORY_BANK_LOAD_FAILED",
            )

        loaded = torch.load(BytesIO(memory_bank_bytes), map_location="cpu")
        payload = self._extract_payload(loaded)
        shape = tuple(payload.shape) if hasattr(payload, "shape") else None
        return LoadedMemoryBank(source_type="torch", payload=payload, shape=shape)

    def _extract_payload(self, loaded: Any) -> Any:
        if isinstance(loaded, dict):
            for key in self._DICT_KEYS:
                if key in loaded:
                    return loaded[key]
        return loaded
