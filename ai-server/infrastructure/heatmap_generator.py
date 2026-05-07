from __future__ import annotations

from io import BytesIO

import cv2
import numpy as np
from PIL import Image


class HeatmapGenerator:
    def render(self, image: np.ndarray, anomaly_map: np.ndarray | None) -> bytes:
        if anomaly_map is None:
            base = image
        else:
            if anomaly_map.shape[:2] != image.shape[:2]:
                anomaly_map = cv2.resize(
                    anomaly_map.astype(np.float32),
                    (image.shape[1], image.shape[0]),
                    interpolation=cv2.INTER_LINEAR,
                )
            normalized = cv2.normalize(anomaly_map, None, 0, 255, cv2.NORM_MINMAX).astype(np.uint8)
            colored = cv2.applyColorMap(normalized, cv2.COLORMAP_JET)
            base = cv2.addWeighted(cv2.cvtColor(image, cv2.COLOR_RGB2BGR), 0.45, colored, 0.55, 0)
            base = cv2.cvtColor(base, cv2.COLOR_BGR2RGB)

        buffer = BytesIO()
        Image.fromarray(base).save(buffer, format="PNG")
        return buffer.getvalue()
