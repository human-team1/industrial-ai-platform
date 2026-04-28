package com.example.factoryguard.application.port.in.document;

import com.example.factoryguard.application.dto.document.DocumentVersionResult;

import java.util.List;

public interface GetDocumentVersionsUseCase {

    List<DocumentVersionResult> execute(Long documentId);
}
