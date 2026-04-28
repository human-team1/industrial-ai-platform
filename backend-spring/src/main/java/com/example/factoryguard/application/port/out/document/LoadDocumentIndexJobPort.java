package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.domain.document.model.DocumentIndexJob;

import java.util.Optional;

public interface LoadDocumentIndexJobPort {

    Optional<DocumentIndexJob> findLatestByDocumentVersionId(Long documentVersionId);
}
