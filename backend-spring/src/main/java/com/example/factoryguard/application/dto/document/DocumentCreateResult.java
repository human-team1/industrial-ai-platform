package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.IndexingStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentCreateResult {

    private final Long documentId;
    private final Long documentVersionId;
    private final IndexingStatus indexingStatus;
}
