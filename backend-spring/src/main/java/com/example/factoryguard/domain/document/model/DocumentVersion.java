package com.example.factoryguard.domain.document.model;

import com.example.factoryguard.domain.document.vo.IndexingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentVersion {

    private final Long documentVersionId;
    private final Long documentId;
    private final Integer versionNo;
    private final Long fileId;
    private final String fileHash;
    private final IndexingStatus indexingStatus;
    private final Integer indexedChunkCount;
    private final String indexErrorMessage;
    private final LocalDateTime indexedAt;
    private final LocalDateTime createdAt;
}
