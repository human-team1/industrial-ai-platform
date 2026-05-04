package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.DocumentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentIndexingTarget {

    private final Long documentId;
    private final Long documentVersionId;
    private final Long fileId;
    private final String fileKey;
    private final DocumentType documentType;
    private final Long organizationId;
}
