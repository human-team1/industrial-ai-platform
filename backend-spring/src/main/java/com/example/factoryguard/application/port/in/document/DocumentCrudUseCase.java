package com.example.factoryguard.application.port.in.document;

import com.example.factoryguard.application.dto.document.CreateDocumentVersionCommand;
import com.example.factoryguard.application.dto.document.CreateDocumentWithFileCommand;
import com.example.factoryguard.application.dto.document.DocumentCreateResult;
import com.example.factoryguard.application.dto.document.DocumentDetailResult;
import com.example.factoryguard.application.dto.document.DocumentListPageResult;
import com.example.factoryguard.application.dto.document.DocumentSearchQuery;
import com.example.factoryguard.application.dto.document.DocumentSummaryResult;
import com.example.factoryguard.application.dto.document.UpdateDocumentMetadataCommand;

public interface DocumentCrudUseCase {

    DocumentListPageResult listDocuments(DocumentSearchQuery query);

    DocumentSummaryResult getSummary();

    DocumentDetailResult getDetail(Long documentId);

    DocumentCreateResult createDocument(CreateDocumentWithFileCommand command);

    DocumentDetailResult updateMetadata(UpdateDocumentMetadataCommand command);

    DocumentCreateResult createVersion(CreateDocumentVersionCommand command);

    void softDelete(Long documentId);
}
