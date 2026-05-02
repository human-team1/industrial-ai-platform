from __future__ import annotations

import hashlib
import re
from dataclasses import dataclass, asdict
from typing import Optional

from rag.loaders.markdown_loader import LoadedDocument


HEADING_PATTERN = re.compile(r"^(#{1,6})\s+(.+?)\s*$")


@dataclass
class ChunkRecord:
    chunk_id: str
    document_id: str
    document_version_id: str
    title: str
    document_type: str
    source_group: str
    relative_path: str
    section_title: Optional[str]
    section_level: Optional[int]
    sequence_no: int
    content: str
    char_count: int
    source_uri: str
    organization_id: str = "1"
    document_status: str = "PUBLISHED"


class SectionChunker:
    def __init__(
        self,
        chunk_size: int = 800,
        chunk_overlap: int = 100,
    ) -> None:
        if chunk_size <= 0:
            raise ValueError("chunk_size must be greater than 0")

        if chunk_overlap < 0:
            raise ValueError("chunk_overlap must be greater than or equal to 0")

        if chunk_overlap >= chunk_size:
            raise ValueError("chunk_overlap must be smaller than chunk_size")

        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap

    def chunk_document(self, document: LoadedDocument) -> list[ChunkRecord]:
        sections = self._split_sections(document.content)

        chunks: list[ChunkRecord] = []
        sequence_no = 1

        document_id = self._make_document_id(document.relative_path)
        document_version_id = f"{document_id}-v1"

        for section in sections:
            section_title = section["title"]
            section_level = section["level"]
            section_content = section["content"].strip()

            if not section_content:
                continue

            split_contents = self._split_long_text(section_content)

            for split_content in split_contents:
                chunk_id = f"{document_id}::chunk_{sequence_no:04d}"

                chunks.append(
                    ChunkRecord(
                        chunk_id=chunk_id,
                        document_id=document_id,
                        document_version_id=document_version_id,
                        title=document.title,
                        document_type=document.document_type,
                        source_group=document.source_group,
                        relative_path=document.relative_path,
                        section_title=section_title,
                        section_level=section_level,
                        sequence_no=sequence_no,
                        content=split_content.strip(),
                        char_count=len(split_content.strip()),
                        source_uri=document.source_uri,
                    )
                )

                sequence_no += 1

        return chunks

    def chunk_documents(self, documents: list[LoadedDocument]) -> list[ChunkRecord]:
        chunks: list[ChunkRecord] = []

        for document in documents:
            chunks.extend(self.chunk_document(document))

        return chunks

    def to_dict(self, chunk: ChunkRecord) -> dict:
        return asdict(chunk)

    def _split_sections(self, content: str) -> list[dict]:
        lines = content.splitlines()

        sections: list[dict] = []
        current_title: Optional[str] = None
        current_level: Optional[int] = None
        current_lines: list[str] = []

        for line in lines:
            match = HEADING_PATTERN.match(line)

            if match:
                if current_lines:
                    sections.append(
                        {
                            "title": current_title,
                            "level": current_level,
                            "content": "\n".join(current_lines),
                        }
                    )

                heading_marks = match.group(1)
                heading_title = match.group(2).strip()

                current_title = heading_title
                current_level = len(heading_marks)
                current_lines = [line]
            else:
                current_lines.append(line)

        if current_lines:
            sections.append(
                {
                    "title": current_title,
                    "level": current_level,
                    "content": "\n".join(current_lines),
                }
            )

        return sections

    def _split_long_text(self, text: str) -> list[str]:
        text = text.strip()

        if len(text) <= self.chunk_size:
            return [text]

        chunks: list[str] = []
        start = 0

        while start < len(text):
            end = start + self.chunk_size
            chunk = text[start:end].strip()

            if chunk:
                chunks.append(chunk)

            if end >= len(text):
                break

            start = end - self.chunk_overlap

        return chunks

    def _make_document_id(self, relative_path: str) -> str:
        digest = hashlib.sha1(relative_path.encode("utf-8")).hexdigest()[:10]
        clean_name = (
            relative_path.replace("\\", "/")
            .replace("/", "-")
            .replace(".md", "")
            .lower()
        )

        clean_name = re.sub(r"[^a-z0-9가-힣_-]+", "-", clean_name)
        clean_name = re.sub(r"-+", "-", clean_name).strip("-")

        return f"doc-{clean_name}-{digest}"