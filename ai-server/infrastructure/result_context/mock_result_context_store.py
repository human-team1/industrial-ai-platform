from __future__ import annotations

import json
from pathlib import Path
from typing import Any


class MockResultContextStore:
    def __init__(self, sample_path: str | Path) -> None:
        self.sample_path = Path(sample_path)
        self.data = self._load()
        # result_id lookup이 자주 일어나므로 시작 시점에 인덱스를 만들어 둔다.
        self.index = self._build_index(self.data)

    def _load(self) -> list[dict[str, Any]]:
        if not self.sample_path.exists():
            raise FileNotFoundError(
                f"result context sample file not found: {self.sample_path}"
            )

        with self.sample_path.open("r", encoding="utf-8") as f:
            data = json.load(f)

        if not isinstance(data, list):
            raise ValueError("result_context_samples_v1.json must be a list")

        return data

    def _build_index(self, data: list[dict[str, Any]]) -> dict[str, dict[str, Any]]:
        index: dict[str, dict[str, Any]] = {}

        for item in data:
            result_id = item.get("result_id")
            if result_id:
                index[str(result_id)] = item

        return index

    def get_by_result_id(self, result_id: str) -> dict[str, Any] | None:
        return self.index.get(result_id)
