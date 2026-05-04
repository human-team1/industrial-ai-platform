package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentIndexRequest {

    private final Long documentId;
    private final Long documentVersionId;
    private final Long fileId;
    private final String fileKey;
    private final String documentType;
    private final Long organizationId;
}
