package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.DocumentIndexJobStatus;
import com.example.factoryguard.domain.document.vo.IndexingStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentIndexingStatusResult {

    private final Long documentVersionId;
    private final Long indexJobId;
    private final IndexingStatus indexingStatus;
    private final Integer indexedChunkCount;
    private final DocumentIndexJobStatus jobStatus;
    private final String errorMessage;
}
