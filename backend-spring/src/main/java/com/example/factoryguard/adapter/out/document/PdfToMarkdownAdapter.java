package com.example.factoryguard.adapter.out.document;

import com.example.factoryguard.application.dto.document.ConvertedDocument;
import com.example.factoryguard.application.port.out.document.ConvertDocumentPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;

@Component
public class PdfToMarkdownAdapter implements ConvertDocumentPort {

    @Override
    public ConvertedDocument convertToMarkdown(byte[] fileBytes, String originalFilename) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(fileBytes))) {
            if (document.isEncrypted()) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "암호화된 PDF는 변환할 수 없습니다.");
            }

            int pageCount = document.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            StringBuilder markdown = new StringBuilder();
            markdown.append("# ").append(stripExtension(originalFilename)).append("\n\n");

            for (int pageNo = 1; pageNo <= pageCount; pageNo++) {
                stripper.setStartPage(pageNo);
                stripper.setEndPage(pageNo);
                String pageText = normalizeText(stripper.getText(document));
                if (pageText.isBlank()) {
                    continue;
                }
                markdown.append("<!-- source_page: ").append(pageNo).append(" -->\n\n");
                markdown.append(pageText).append("\n\n");
            }

            if (markdown.toString().replace("# " + stripExtension(originalFilename), "").trim().isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "PDF에서 추출 가능한 텍스트가 없습니다.");
            }

            return ConvertedDocument.builder()
                    .markdown(markdown.toString().trim() + "\n")
                    .convertedFileName(toMarkdownFileName(originalFilename))
                    .mimeType("text/markdown")
                    .pageCount(pageCount)
                    .build();
        } catch (BusinessException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "PDF 파일을 읽거나 변환할 수 없습니다.");
        }
    }

    private String normalizeText(String text) {
        return text == null ? "" : text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+\\n", "\n")
                .trim();
    }

    private String toMarkdownFileName(String originalFilename) {
        return stripExtension(originalFilename) + ".md";
    }

    private String stripExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "uploaded-document";
        }
        String normalized = originalFilename.trim();
        int dotIndex = normalized.lastIndexOf('.');
        if (dotIndex <= 0) {
            return normalized;
        }
        return normalized.substring(0, dotIndex);
    }
}
