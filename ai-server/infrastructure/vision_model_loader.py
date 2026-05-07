from datetime import datetime

import torch

from application.exceptions import AppException
from domain.vision_models import LoadedVisionModel
from infrastructure.vision.anomalib_patchcore_loader import load_patchcore_ckpt


class VisionModelLoader:
    def __init__(self) -> None:
        self._cache: dict[str, LoadedVisionModel] = {}

    def load(
        self,
        model_version_id: int,
        ckpt_file_key: str,
        config_file_key: str,
        memory_bank_file_key: str,
        ckpt_bytes: bytes,
        config: dict,
    ) -> LoadedVisionModel:
        cache_key = f"{model_version_id}:{ckpt_file_key}:{config_file_key}:{memory_bank_file_key}"
        cached = self._cache.get(cache_key)
        if cached is not None:
            return cached

        device = "cuda" if torch.cuda.is_available() else "cpu"
        runtime_model = self._load_anomalib_model(ckpt_bytes, device)

        loaded = LoadedVisionModel(
            model_version_id=model_version_id,
            ckpt_file_key=ckpt_file_key,
            config_file_key=config_file_key,
            memory_bank_file_key=memory_bank_file_key,
            ckpt_bytes=ckpt_bytes,
            config=config,
            loaded_at=datetime.now(),
            runtime_model=runtime_model,
            runtime_metadata={
                "backend": "anomalib",
                "anomalibVersion": "2.4.0",
                "device": device,
            },
        )
        self._cache[cache_key] = loaded
        return loaded

    def _load_anomalib_model(self, ckpt_bytes: bytes, device: str):
        try:
            return load_patchcore_ckpt(ckpt_bytes, device)
        except Exception as exc:
            raise AppException(
                500,
                "Anomalib model load failed",
                "Anomalib 체크포인트 로드에 실패했습니다.",
                "ANOMALIB_LOAD_FAILED",
            ) from exc
