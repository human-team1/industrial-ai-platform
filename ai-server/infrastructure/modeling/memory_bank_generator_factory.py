from __future__ import annotations

import json
from pathlib import Path

from application.exceptions import AppException
from domain.models.memory_bank import MemoryBankProfileSpec, ModelCategory, ModelProfile
from domain.vision_model_profile import get_vision_model_profile_spec
from infrastructure.modeling.patchcore_memory_bank_generator import PatchCoreMemoryBankGenerator


CONFIG_DIR = Path(__file__).resolve().parents[2] / "config" / "model"

CONFIG_FILES: dict[tuple[ModelCategory, ModelProfile], str] = {
    (ModelCategory.OBJECT, ModelProfile.SPEED): "object_speed_wrn50_layer2_224_full_mb10000.json",
    (ModelCategory.OBJECT, ModelProfile.PERFORMANCE): "object_perf_dinov2_base_3layers_336_50shot_mb2000.json",
    (ModelCategory.TEXTURE, ModelProfile.SPEED): "texture_speed_wrn50_layer2_256_full_mb5000.json",
    (ModelCategory.TEXTURE, ModelProfile.PERFORMANCE): "texture_perf_dinov2_base_3layers_448_50shot_mb2000.json",
}


class MemoryBankGeneratorFactory:
    def load_config(self, category: ModelCategory, profile: ModelProfile) -> tuple[dict, MemoryBankProfileSpec]:
        filename = CONFIG_FILES.get((category, profile))
        if filename is None:
            raise AppException(404, "Model config not found", "profile config mapping was not found.", "MODEL_CONFIG_NOT_FOUND")
        path = CONFIG_DIR / filename
        if not path.is_file():
            raise AppException(404, "Model config not found", f"local model config not found: {path}", "MODEL_CONFIG_NOT_FOUND")
        try:
            config = json.loads(path.read_text(encoding="utf-8"))
            spec = self._to_spec(category, profile, config)
        except AppException:
            raise
        except Exception as exc:
            raise AppException(422, "Invalid model config", f"invalid local model config: {path.name}", "MODEL_CONFIG_INVALID") from exc
        config.setdefault("modelCategory", category.value)
        config.setdefault("modelProfile", profile.value)
        config["configFileName"] = path.name
        return config, spec

    def create_from_spec(self, spec: MemoryBankProfileSpec) -> PatchCoreMemoryBankGenerator:
        return PatchCoreMemoryBankGenerator(spec)

    def to_spec(self, category: ModelCategory, profile: ModelProfile, config: dict) -> MemoryBankProfileSpec:
        profile_spec = get_vision_model_profile_spec(category.value, profile.value)
        pipeline = str(config.get("modelPipeline") or config.get("pipeline") or "PatchCore")
        return MemoryBankProfileSpec(
            category=category,
            profile=profile,
            pipeline=pipeline,
            image_size=(profile_spec.input_size, profile_spec.input_size),
            target_memory_bank_size=int(profile_spec.target_memory_bank_size),
            layers=tuple(profile_spec.patchcore_layers),
            shot_policy=profile_spec.shot_policy,
            image_threshold=float(profile_spec.image_threshold),
            pixel_threshold=float(profile_spec.pixel_threshold),
            framework="PYTORCH",
        )
