package com.example.factoryguard.application.port.in.document;

import com.example.factoryguard.application.dto.document.DocumentIndexingStatusResult;

public interface GetDocumentIndexingStatusUseCase {

    DocumentIndexingStatusResult execute(Long documentVersionId);
}
