package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.IndexingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentListItem {

    private final Long documentId;
    private final String title;
    private final String documentType;
    private final String category;
    private final String equipmentType;
    private final String authorName;
    private final IndexingStatus indexingStatus;
    private final Integer versionNo;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime lastUsedAt;
    private final Long fileSize;
    private final String fileName;
}
