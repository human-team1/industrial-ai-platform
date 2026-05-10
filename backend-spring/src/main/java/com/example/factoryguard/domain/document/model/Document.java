package com.example.factoryguard.domain.document.model;

import com.example.factoryguard.domain.document.vo.DocumentStatus;
import com.example.factoryguard.domain.document.vo.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Document {

    private final Long documentId;
    private final Long organizationId;
    private final Long ownerUserId;
    private final String title;
    private final DocumentType documentType;
    private final DocumentStatus currentStatus;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
}
