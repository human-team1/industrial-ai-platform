package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.domain.document.model.Document;

public interface SaveDocumentPort {

    Document save(Document document);
}
