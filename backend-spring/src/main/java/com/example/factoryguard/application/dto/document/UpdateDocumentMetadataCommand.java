package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateDocumentMetadataCommand {

    private final Long userId;
    private final Long organizationId;
    private final Long documentId;
    private final String title;
    private final String category;
    private final String equipmentType;
    private final String description;
    private final String tags;
}
