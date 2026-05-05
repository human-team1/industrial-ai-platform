package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.application.dto.document.DocumentIndexRequest;
import com.example.factoryguard.application.dto.document.DocumentIndexResponse;

public interface CallDocumentIndexingPort {

    DocumentIndexResponse index(DocumentIndexRequest request, String requestId);
}
