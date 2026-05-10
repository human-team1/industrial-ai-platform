package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.application.dto.document.DocumentUploadResult;
import com.example.factoryguard.domain.document.vo.DocumentType;
import com.example.factoryguard.domain.document.vo.IndexingStatus;

public interface SaveUploadedDocumentPort {

    DocumentUploadResult save(
            Long organizationId,
            Long ownerUserId,
            String title,
            DocumentType documentType,
            Long originalFileId,
            String fileHash,
            IndexingStatus indexingStatus
    );
}
