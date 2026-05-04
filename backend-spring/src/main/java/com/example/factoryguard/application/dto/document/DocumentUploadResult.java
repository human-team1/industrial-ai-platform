package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentUploadResult {

    private final Long documentId;
    private final Long documentVersionId;
    private final Long fileId;
    private final Long indexJobId;
    private final String indexingStatus;
}
