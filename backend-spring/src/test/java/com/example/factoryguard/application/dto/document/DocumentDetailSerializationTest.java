package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.DocumentStatus;
import com.example.factoryguard.domain.document.vo.IndexingStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DocumentDetailSerializationTest {

    @Test
    void jacksonCanSerializeDocumentDetailResult() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        DocumentDetailResult detail = DocumentDetailResult.builder()
                .documentId(1L)
                .title("t")
                .documentType("TXT")
                .category(null)
                .equipmentType(null)
                .description(null)
                .tags(List.of())
                .ownerUserId(1L)
                .authorName(null)
                .currentStatus(DocumentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .latestVersion(DocumentVersionDetailResult.builder()
                        .documentVersionId(1L)
                        .versionNo(1)
                        .fileId(1L)
                        .fileName("a.txt")
                        .fileSize(10L)
                        .fileExt("txt")
                        .mimeType("text/plain")
                        .fileHash("abc")
                        .indexingStatus(IndexingStatus.PENDING)
                        .indexedChunkCount(0)
                        .indexErrorMessage(null)
                        .indexedAt(null)
                        .createdAt(LocalDateTime.now())
                        .build())
                .build();
        assertDoesNotThrow(() -> mapper.writeValueAsString(detail));
    }
}
