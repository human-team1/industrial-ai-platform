from __future__ import annotations

from io import BytesIO

import numpy as np
from PIL import Image

from application.exceptions import AppException
from domain.vision_models import PreprocessedImage


class VisionImagePreprocessor:
    def preprocess(self, image_bytes: bytes, roi: dict | None, input_size: str | None, config: dict) -> PreprocessedImage:
        try:
            image = Image.open(BytesIO(image_bytes)).convert("RGB")
        except Exception as exc:
            raise AppException(500, "Image decode failed", "이미지 디코딩 중 오류가 발생했습니다.", "AI_IMAGE_DECODE_FAILED") from exc

        original_width, original_height = image.size
        image = self._apply_roi(image, roi, original_width, original_height)
        resize_to = self._resolve_input_size(input_size, config)
        resized = image.resize(resize_to)
        return PreprocessedImage(
            image_array=np.asarray(resized, dtype=np.uint8),
            original_size=(original_width, original_height),
            resized_size=resize_to,
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

    def _resolve_input_size(self, request_input_size: str | None, config: dict) -> tuple[int, int]:
        size_value = request_input_size or config.get("inputSize") or config.get("input_size") or "256x256"
        if isinstance(size_value, list) and len(size_value) == 2:
            return int(size_value[0]), int(size_value[1])
        if isinstance(size_value, str) and "x" in size_value.lower():
            width, height = size_value.lower().split("x", 1)
            return int(width), int(height)
        raise AppException(500, "Invalid input size", "config 또는 요청의 input size 형식이 올바르지 않습니다.", "AI_CONFIG_PARSE_FAILED")
