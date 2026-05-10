package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.domain.document.model.DocumentVersion;

import java.util.List;
import java.util.Optional;

public interface LoadDocumentVersionPort {

    Optional<DocumentVersion> findById(Long documentVersionId);

    List<DocumentVersion> findAllByDocumentId(Long documentId);
}
