package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.DocumentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DocumentDetailResult {

    private final Long documentId;
    private final String title;
    private final String documentType;
    private final String category;
    private final String equipmentType;
    private final String description;
    private final List<String> tags;
    private final Long ownerUserId;
    private final String authorName;
    private final DocumentStatus currentStatus;
    private final DocumentVersionDetailResult latestVersion;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
