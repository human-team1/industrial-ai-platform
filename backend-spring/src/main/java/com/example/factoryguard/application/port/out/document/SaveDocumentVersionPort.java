package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.domain.document.model.DocumentVersion;

public interface SaveDocumentVersionPort {

    DocumentVersion save(DocumentVersion documentVersion);
}
