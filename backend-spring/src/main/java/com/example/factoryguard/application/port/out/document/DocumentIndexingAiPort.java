package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.application.dto.document.DocumentDeindexRequest;
import com.example.factoryguard.application.dto.document.DocumentDeindexResponse;
import com.example.factoryguard.application.dto.document.DocumentIndexRequest;
import com.example.factoryguard.application.dto.document.DocumentIndexResponse;

public interface DocumentIndexingAiPort {

    DocumentIndexResponse enqueue(DocumentIndexRequest request, String requestId);

    DocumentIndexResponse getStatus(String aiJobId);

    DocumentDeindexResponse deindex(Long documentVersionId, DocumentDeindexRequest request, String requestId);
}
