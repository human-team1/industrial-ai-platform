package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.domain.document.model.Document;

import java.util.List;
import java.util.Optional;

public interface LoadDocumentPort {

    Optional<Document> findById(Long documentId);

    List<Document> findAllByOrganizationId(Long organizationId);
}
