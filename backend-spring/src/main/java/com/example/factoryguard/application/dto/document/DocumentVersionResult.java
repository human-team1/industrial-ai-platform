package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.IndexingStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentVersionResult {

    private final Long documentVersionId;
    private final Long documentId;
    private final Integer versionNo;
    private final Long fileId;
    private final IndexingStatus indexingStatus;
    private final Integer indexedChunkCount;
}
