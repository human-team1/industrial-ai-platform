from __future__ import annotations

from io import BytesIO

import numpy as np
from PIL import Image

from application.exceptions import AppException
from domain.vision_model_profile import get_vision_model_profile_spec
from domain.vision_models import PreprocessedImage
from infrastructure.vision.preprocessing import preprocess_pil_image


class VisionImagePreprocessor:
    def preprocess(self, image_bytes: bytes, roi: dict | None, model_category: str, model_profile: str) -> PreprocessedImage:
        try:
            image = Image.open(BytesIO(image_bytes)).convert("RGB")
        except Exception as exc:
            raise AppException(500, "Image decode failed", "이미지 디코딩 중 오류가 발생했습니다.", "AI_IMAGE_DECODE_FAILED") from exc

        original_width, original_height = image.size
        image = self._apply_roi(image, roi, original_width, original_height)
        try:
            spec = get_vision_model_profile_spec(model_category, model_profile)
        except ValueError as exc:
            raise AppException(422, "Invalid model profile", "지원하지 않는 모델 프로필 조합입니다.", "AI_MODEL_PROFILE_INVALID") from exc
        preprocessed = preprocess_pil_image(image, spec)
        return PreprocessedImage(
            image_array=np.asarray(preprocessed.resized_rgb_uint8, dtype=np.uint8),
            original_image_array=np.asarray(image.convert("RGB"), dtype=np.uint8),
            original_size=(original_width, original_height),
            resized_size=(spec.input_size, spec.input_size),
        )

    def _apply_roi(self, image: Image.Image, roi: dict | None, width: int, height: int) -> Image.Image:
        if not roi or roi.get("roiMode", "FULL_FRAME") == "FULL_FRAME":
            return image

        try:
            left = int(roi["roiX"] * width)
            top = int(roi["roiY"] * height)
            crop_width = int(roi["roiWidth"] * width)
            crop_height = int(roi["roiHeight"] * height)
        except Exception as exc:
            raise AppException(422, "Invalid ROI", "ROI 좌표가 올바르지 않습니다.", "AI_ROI_INVALID") from exc

        right = left + crop_width
        bottom = top + crop_height
        if left < 0 or top < 0 or right > width or bottom > height or crop_width <= 0 or crop_height <= 0:
            raise AppException(422, "Invalid ROI", "ROI 영역이 이미지 범위를 벗어났습니다.", "AI_ROI_INVALID")
        return image.crop((left, top, right, bottom))

