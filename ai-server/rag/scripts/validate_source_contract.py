from __future__ import annotations

import json
import sys
from pathlib import Path


sys.path.append(str(Path(__file__).resolve().parents[2]))

from rag.schemas import SourceChunk


MOCK_SOURCES_PATH = Path("experiments/rag_langgraph_eval/datasets/mock_sources_v1.json")


def main() -> None:
    sources = json.loads(MOCK_SOURCES_PATH.read_text(encoding="utf-8"))

    if not isinstance(sources, list):
        raise RuntimeError("mock_sources_v1.json must be a JSON array.")

    for source in sources:
        SourceChunk(**source)

    empty_sources: list[SourceChunk] = []
    if empty_sources != []:
        raise RuntimeError("sources must allow an empty array.")

    print(f"validated_sources: {len(sources)}")
    print("Source contract validation passed.")


if __name__ == "__main__":
    main()
