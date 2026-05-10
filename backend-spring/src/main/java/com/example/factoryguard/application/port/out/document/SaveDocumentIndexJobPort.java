package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.domain.document.model.DocumentIndexJob;

public interface SaveDocumentIndexJobPort {

    DocumentIndexJob save(DocumentIndexJob documentIndexJob);
}
