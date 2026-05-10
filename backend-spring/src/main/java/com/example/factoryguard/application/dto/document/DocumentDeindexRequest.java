package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentDeindexRequest {

    private final Long organizationId;
    private final Long documentId;
    private final String reason;
}
