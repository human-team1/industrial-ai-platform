from __future__ import annotations

import csv
from dataclasses import dataclass
from pathlib import Path


EXCLUDE_NAMES = {
    "00_INDEX.md",
    "README_RAG.md",
    "documents_metadata.csv",
    "documents_metadata.json",
    "RAG_PDF_압축_AI작업지시서.md",
}


@dataclass
class LoadedDocument:
    relative_path: str
    source_group: str
    file_name: str
    title: str
    document_type: str
    content: str
    source_uri: str


class MarkdownLoader:
    def __init__(
        self,
        corpus_dir: str = "rag/corpus",
        catalog_path: str = "rag/outputs/document_catalog.csv",
    ) -> None:
        self.corpus_dir = Path(corpus_dir)
        self.catalog_path = Path(catalog_path)
        self.catalog_by_path = self._load_catalog()

    def load(self) -> list[LoadedDocument]:
        if not self.corpus_dir.exists():
            raise FileNotFoundError(f"Corpus directory not found: {self.corpus_dir}")

        documents: list[LoadedDocument] = []

        for path in sorted(self.corpus_dir.rglob("*.md")):
            if not self._should_include(path):
                continue

            relative_path = self._normalize_path(path.relative_to(self.corpus_dir))
            content = path.read_text(encoding="utf-8", errors="ignore")
            catalog_row = self.catalog_by_path.get(relative_path, {})

            title = catalog_row.get("title") or self._detect_title(content, path.stem)
            document_type = catalog_row.get("document_type") or "UNKNOWN"
            source_group = catalog_row.get("source_group") or self._detect_source_group(relative_path)

            documents.append(
                LoadedDocument(
                    relative_path=relative_path,
                    source_group=source_group,
                    file_name=path.name,
                    title=title,
                    document_type=document_type,
                    content=content,
                    source_uri=str(path),
                )
            )

        if not documents:
            raise RuntimeError(f"No markdown documents found in {self.corpus_dir}")

        return documents

    def _load_catalog(self) -> dict[str, dict[str, str]]:
        if not self.catalog_path.exists():
            return {}

        rows: dict[str, dict[str, str]] = {}

        with self.catalog_path.open("r", encoding="utf-8-sig", newline="") as f:
            reader = csv.DictReader(f)

            for row in reader:
                relative_path = self._normalize_path(row["relative_path"])
                rows[relative_path] = row

        return rows

    def _should_include(self, path: Path) -> bool:
        if not path.is_file():
            return False

        if path.name in EXCLUDE_NAMES:
            return False

        return path.suffix.lower() == ".md"

    def _detect_title(self, content: str, fallback: str) -> str:
        for line in content.splitlines():
            stripped = line.strip()

            if stripped.startswith("# "):
                return stripped.replace("# ", "").strip()

        return fallback.replace("_", " ")

    def _detect_source_group(self, relative_path: str) -> str:
        parts = relative_path.split("/")

        if not parts:
            return "UNKNOWN"

        return parts[0]

    def _normalize_path(self, path: Path | str) -> str:
        return str(path).replace("\\", "/")