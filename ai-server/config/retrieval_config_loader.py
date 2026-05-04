from __future__ import annotations

from pathlib import Path
from typing import Any

import yaml

from domain.rag.retrieval_config import RetrievalConfig


def load_retrieval_config(config_path: str | Path) -> RetrievalConfig:
    path = Path(config_path)

    if not path.exists():
        raise FileNotFoundError(f"retrieval config file not found: {path}")

    with path.open("r", encoding="utf-8") as f:
        raw_config: dict[str, Any] = yaml.safe_load(f) or {}

    return RetrievalConfig.model_validate(raw_config)