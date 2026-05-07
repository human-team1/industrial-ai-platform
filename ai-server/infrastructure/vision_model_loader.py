from datetime import datetime
from io import BytesIO

import torch
from anomalib.models.image import Patchcore

from application.exceptions import AppException
from domain.vision_models import LoadedVisionModel


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

        loaded = LoadedVisionModel(
            model_version_id=model_version_id,
            ckpt_file_key=ckpt_file_key,
            config_file_key=config_file_key,
            memory_bank_file_key=memory_bank_file_key,
            ckpt_bytes=ckpt_bytes,
            config=config,
            loaded_at=datetime.now(),
            runtime_model=self._load_anomalib_model(ckpt_bytes),
            runtime_metadata={"backend": "anomalib", "anomalibVersion": "2.4.0", "device": "cuda" if torch.cuda.is_available() else "cpu"},
        )
        self._cache[cache_key] = loaded
        return loaded

    def _load_anomalib_model(self, ckpt_bytes: bytes):
        device = "cuda" if torch.cuda.is_available() else "cpu"
        try:
            model = Patchcore.load_from_checkpoint(
                checkpoint_path=BytesIO(ckpt_bytes),
                map_location=device,
                strict=False,
            )
            model.to(device)
            model.eval()
            return model
        except TypeError:
            # load_from_checkpoint expects path-like; fallback to temp file flow.
            import tempfile

            with tempfile.NamedTemporaryFile(suffix=".ckpt", delete=True) as temp_ckpt:
                temp_ckpt.write(ckpt_bytes)
                temp_ckpt.flush()
                model = Patchcore.load_from_checkpoint(temp_ckpt.name, map_location=device, strict=False)
                model.to(device)
                model.eval()
                return model
        except Exception as exc:
            raise AppException(500, "Anomalib model load failed", "Anomalib 체크포인트 로드에 실패했습니다.", "ANOMALIB_LOAD_FAILED") from exc
