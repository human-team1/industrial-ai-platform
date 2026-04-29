package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.application.dto.document.DocumentCreateResult;
import com.example.factoryguard.application.dto.document.DocumentDetailResult;
import com.example.factoryguard.application.dto.document.DocumentListPageResult;
import com.example.factoryguard.application.dto.document.DocumentSearchQuery;
import com.example.factoryguard.application.dto.document.DocumentSummaryResult;
import com.example.factoryguard.application.dto.document.DocumentVersionDetailResult;

import java.util.Optional;

public interface DocumentCrudPort {

    DocumentListPageResult findDocuments(DocumentSearchQuery query, Long organizationId, boolean isAdmin);

    DocumentSummaryResult summarize(Long organizationId, boolean isAdmin);

    Optional<DocumentDetailResult> findDetail(Long documentId, Long organizationId, boolean isAdmin);

    Optional<Long> findDocumentOrganizationId(Long documentId);

    DocumentCreateResult createDocument(Long organizationId, Long ownerUserId, String title, String documentType,
                                        String category, String equipmentType, String description, String tags,
                                        Long fileId, String authorName);

    DocumentDetailResult updateMetadata(Long documentId, Long organizationId, boolean isAdmin, String title,
                                        String category, String equipmentType, String description, String tags);

    DocumentVersionDetailResult createVersion(Long documentId, Long organizationId, boolean isAdmin, Long fileId,
                                              String fileHash, String changeReason);

    boolean softDelete(Long documentId, Long organizationId, boolean isAdmin);
}
