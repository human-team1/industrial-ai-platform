package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.DocumentStatus;
import com.example.factoryguard.domain.document.vo.DocumentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentDetail {

    private final Long documentId;
    private final Long organizationId;
    private final Long ownerUserId;
    private final String title;
    private final DocumentType documentType;
    private final DocumentStatus currentStatus;
}
