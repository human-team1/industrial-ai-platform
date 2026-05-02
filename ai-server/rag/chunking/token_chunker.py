from __future__ import annotations

import re

from rag.chunking.section_chunker import ChunkRecord, SectionChunker
from rag.loaders.markdown_loader import LoadedDocument


TOKEN_PATTERN = re.compile(r"\S+")


class TokenChunker(SectionChunker):
    """Whitespace token based chunker that preserves ChunkRecord shape."""

    def chunk_document(self, document: LoadedDocument) -> list[ChunkRecord]:
        tokens = TOKEN_PATTERN.findall(document.content)
        chunks: list[ChunkRecord] = []
        sequence_no = 1

        document_id = self._make_document_id(document.relative_path)
        document_version_id = f"{document_id}-v1"

        start = 0
        while start < len(tokens):
            end = start + self.chunk_size
            content = " ".join(tokens[start:end]).strip()

            if content:
                chunks.append(
                    ChunkRecord(
                        chunk_id=f"{document_id}::chunk_{sequence_no:04d}",
                        document_id=document_id,
                        document_version_id=document_version_id,
                        title=document.title,
                        document_type=document.document_type,
                        source_group=document.source_group,
                        relative_path=document.relative_path,
                        section_title=None,
                        section_level=None,
                        sequence_no=sequence_no,
                        content=content,
                        char_count=len(content),
                        source_uri=document.source_uri,
                    )
                )
                sequence_no += 1

            if end >= len(tokens):
                break

            start = max(end - self.chunk_overlap, start + 1)

        return chunks
