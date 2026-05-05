package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentIndexJobPollingTarget {

    private final Long indexJobId;
    private final String aiJobId;
    private final Long documentId;
    private final Long documentVersionId;
}
