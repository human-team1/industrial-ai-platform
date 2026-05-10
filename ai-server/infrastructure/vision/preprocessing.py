from __future__ import annotations

from dataclasses import dataclass

import numpy as np
from PIL import Image

from domain.vision_model_profile import IMAGENET_MEAN, IMAGENET_STD, VisionModelProfileSpec


@dataclass(frozen=True)
class PreprocessedVisionImage:
    normalized_chw: np.ndarray
    resized_rgb_uint8: np.ndarray
    original_width: int
    original_height: int
    input_size: int


def preprocess_pil_image(image: Image.Image, spec: VisionModelProfileSpec) -> PreprocessedVisionImage:
    rgb_image = image.convert("RGB")
    original_width, original_height = rgb_image.size
    resized = rgb_image.resize((spec.input_size, spec.input_size))
    resized_rgb = np.asarray(resized, dtype=np.uint8)
    normalized = resized_rgb.astype(np.float32) / 255.0
    normalized = (normalized - np.asarray(IMAGENET_MEAN, dtype=np.float32)) / np.asarray(IMAGENET_STD, dtype=np.float32)
    normalized_chw = np.transpose(normalized, (2, 0, 1))
    return PreprocessedVisionImage(
        normalized_chw=normalized_chw,
        resized_rgb_uint8=resized_rgb,
        original_width=original_width,
        original_height=original_height,
        input_size=spec.input_size,
    )
