package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.IndexingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentVersionDetailResult {

    private final Long documentVersionId;
    private final Integer versionNo;
    private final Long fileId;
    private final String fileName;
    private final Long fileSize;
    private final String fileExt;
    private final String mimeType;
    private final String fileHash;
    private final IndexingStatus indexingStatus;
    private final Integer indexedChunkCount;
    private final String indexErrorMessage;
    private final LocalDateTime indexedAt;
    private final LocalDateTime createdAt;
}
