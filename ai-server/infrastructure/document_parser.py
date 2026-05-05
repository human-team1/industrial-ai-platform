import io
import re

from application.exceptions import AppException
from domain.document_models import ParsedSection


class DocumentParser:
    def parse(self, content: bytes, document_type: str) -> list[ParsedSection]:
        if document_type == "PDF":
            return self._parse_pdf(content)
        if document_type == "DOCX":
            return self._parse_docx(content)
        text = self._decode_text(content)
        return self._preprocess([ParsedSection(content=text)])

    def _parse_pdf(self, content: bytes) -> list[ParsedSection]:
        from pypdf import PdfReader

        try:
            reader = PdfReader(io.BytesIO(content))
            sections = [
                ParsedSection(content=page.extract_text() or "", page_no=index + 1)
                for index, page in enumerate(reader.pages)
            ]
        except Exception as exc:
            raise AppException(422, "Document parse failed", "PDF 문서 파싱에 실패했습니다.", "DOCUMENT-422") from exc
        return self._preprocess(sections)

    def _parse_docx(self, content: bytes) -> list[ParsedSection]:
        from docx import Document

        try:
            document = Document(io.BytesIO(content))
            text = "\n".join(paragraph.text for paragraph in document.paragraphs)
        except Exception as exc:
            raise AppException(422, "Document parse failed", "DOCX 문서 파싱에 실패했습니다.", "DOCUMENT-422") from exc
        return self._preprocess([ParsedSection(content=text)])

    def _decode_text(self, content: bytes) -> str:
        try:
            return content.decode("utf-8")
        except UnicodeDecodeError:
            return content.decode("cp949", errors="ignore")

    def _preprocess(self, sections: list[ParsedSection]) -> list[ParsedSection]:
        cleaned: list[ParsedSection] = []
        for section in sections:
            text = re.sub(r"\s+", " ", section.content).strip()
            if len(text) < 10:
                continue
            cleaned.append(ParsedSection(content=text, page_no=section.page_no, section=section.section))
        if not cleaned:
            raise AppException(422, "Empty document", "인덱싱할 수 있는 문서 텍스트가 없습니다.", "DOCUMENT-422")
        return cleaned
