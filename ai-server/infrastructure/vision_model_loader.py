from datetime import datetime

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
        )
        self._cache[cache_key] = loaded
        return loaded
