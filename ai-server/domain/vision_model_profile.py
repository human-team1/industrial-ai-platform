from __future__ import annotations

from dataclasses import dataclass


IMAGENET_MEAN = (0.485, 0.456, 0.406)
IMAGENET_STD = (0.229, 0.224, 0.225)


@dataclass(frozen=True)
class VisionModelProfileSpec:
    model_category: str
    model_profile: str
    backbone: str
    patchcore_layers: tuple[str, ...]
    input_size: int
    shot_policy: str
    target_memory_bank_size: int
    image_threshold: float
    pixel_threshold: float
    normalize: str = "imagenet"


VISION_MODEL_PROFILE_SPECS: dict[tuple[str, str], VisionModelProfileSpec] = {
    ("OBJECT", "PERFORMANCE"): VisionModelProfileSpec(
        model_category="OBJECT",
        model_profile="PERFORMANCE",
        backbone="dinov2-base",
        patchcore_layers=("blocks.3", "blocks.7", "blocks.11"),
        input_size=336,
        shot_policy="50-shot",
        target_memory_bank_size=2000,
        image_threshold=7.4762,
        pixel_threshold=6.6040,
    ),
    ("TEXTURE", "PERFORMANCE"): VisionModelProfileSpec(
        model_category="TEXTURE",
        model_profile="PERFORMANCE",
        backbone="dinov2-base",
        patchcore_layers=("blocks.3", "blocks.7", "blocks.11"),
        input_size=448,
        shot_policy="50-shot",
        target_memory_bank_size=2000,
        image_threshold=22.2103,
        pixel_threshold=25.827,
    ),
    ("OBJECT", "SPEED"): VisionModelProfileSpec(
        model_category="OBJECT",
        model_profile="SPEED",
        backbone="wide_resnet50",
        patchcore_layers=("layer2",),
        input_size=224,
        shot_policy="full-shot",
        target_memory_bank_size=10000,
        image_threshold=36.8688,
        pixel_threshold=28.4368,
    ),
    ("TEXTURE", "SPEED"): VisionModelProfileSpec(
        model_category="TEXTURE",
        model_profile="SPEED",
        backbone="wide_resnet50",
        patchcore_layers=("layer2",),
        input_size=256,
        shot_policy="full-shot",
        target_memory_bank_size=5000,
        image_threshold=40.0642,
        pixel_threshold=36.5779,
    ),
}


def get_vision_model_profile_spec(model_category: str, model_profile: str) -> VisionModelProfileSpec:
    key = ((model_category or "").upper(), (model_profile or "").upper())
    spec = VISION_MODEL_PROFILE_SPECS.get(key)
    if spec is None:
        raise ValueError(f"Unsupported vision model profile: {key}")
    return spec
