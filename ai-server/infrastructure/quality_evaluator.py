from __future__ import annotations

import cv2
import numpy as np

from domain.vision_models import QualityMetrics


class QualityEvaluator:
    def evaluate(self, image_array: np.ndarray) -> QualityMetrics:
        gray = cv2.cvtColor(image_array, cv2.COLOR_RGB2GRAY)
        hsv = cv2.cvtColor(image_array, cv2.COLOR_RGB2HSV)

        brightness = float(gray.mean())
        contrast = float(gray.std())
        blur_score = float(cv2.Laplacian(gray, cv2.CV_64F).var())
        saturation = float(hsv[:, :, 1].mean())

        reason = None
        status = "PASSED"
        if brightness < 30:
            status, reason = "FAILED", "TOO_DARK"
        elif brightness > 235:
            status, reason = "FAILED", "TOO_BRIGHT"
        elif contrast < 10:
            status, reason = "FAILED", "LOW_CONTRAST"
        elif blur_score < 30:
            status, reason = "FAILED", "BLURRY"
        elif contrast < 20 or blur_score < 60:
            status = "WARNING"

        return QualityMetrics(
            status=status,
            reason=reason,
            brightness=brightness,
            contrast=contrast,
            blur_score=blur_score,
            saturation=saturation,
        )
