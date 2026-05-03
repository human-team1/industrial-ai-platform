import json
from typing import Any

from application.exceptions import AppException


class VisionConfigLoader:
    def load(self, config_bytes: bytes) -> dict[str, Any]:
        try:
            data = json.loads(config_bytes.decode("utf-8"))
        except (UnicodeDecodeError, json.JSONDecodeError) as exc:
            raise AppException(500, "Config parse failed", "config.json 파싱 중 오류가 발생했습니다.", "AI_CONFIG_PARSE_FAILED") from exc

        if not isinstance(data, dict):
            raise AppException(500, "Config parse failed", "config.json 형식이 올바르지 않습니다.", "AI_CONFIG_PARSE_FAILED")
        return data
