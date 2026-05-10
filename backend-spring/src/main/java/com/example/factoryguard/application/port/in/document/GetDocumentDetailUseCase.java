package com.example.factoryguard.application.port.in.document;

import com.example.factoryguard.application.dto.document.DocumentDetail;

public interface GetDocumentDetailUseCase {

    DocumentDetail execute(Long documentId);
}
