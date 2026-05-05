package com.example.factoryguard.adapter.out.document;

import com.example.factoryguard.application.dto.document.ConvertedDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class PdfToMarkdownAdapterTest {

    private final PdfToMarkdownAdapter adapter = new PdfToMarkdownAdapter();

    @Test
    void convertToMarkdownExtractsTextWithPageMarker() throws Exception {
        byte[] pdfBytes = createPdf("Motor overheating checklist");

        ConvertedDocument result = adapter.convertToMarkdown(pdfBytes, "motor-manual.pdf");

        assertThat(result.getConvertedFileName()).isEqualTo("motor-manual.md");
        assertThat(result.getMimeType()).isEqualTo("text/markdown");
        assertThat(result.getPageCount()).isEqualTo(1);
        assertThat(result.getMarkdown()).contains("# motor-manual");
        assertThat(result.getMarkdown()).contains("<!-- source_page: 1 -->");
        assertThat(result.getMarkdown()).contains("Motor overheating checklist");
    }

    private byte[] createPdf(String text) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(72, 720);
                contentStream.showText(text);
                contentStream.endText();
            }
            document.save(out);
            return out.toByteArray();
        }
    }
}
