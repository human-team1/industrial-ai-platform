package com.example.factoryguard.adapter.out.persistence.document.mapper;

import com.example.factoryguard.adapter.out.persistence.document.entity.DocumentEntity;
import com.example.factoryguard.adapter.out.persistence.document.entity.DocumentVersionEntity;
import com.example.factoryguard.domain.document.model.Document;
import com.example.factoryguard.domain.document.model.DocumentVersion;
import com.example.factoryguard.domain.document.vo.DocumentStatus;
import com.example.factoryguard.domain.document.vo.DocumentType;
import com.example.factoryguard.domain.document.vo.IndexingStatus;

public class DocumentMapper {

    // DocumentEntity → Document 도메인
    public static Document toDomain(DocumentEntity entity) {
        return Document.builder()
                .documentId(entity.getDocumentId())
                .organizationId(entity.getOrganizationId())
                .ownerUserId(entity.getOwnerUserId())
                .title(entity.getTitle())
                .documentType(DocumentType.valueOf(entity.getDocumentType()))
                .currentStatus(DocumentStatus.valueOf(entity.getCurrentStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    // Document 도메인 → DocumentEntity
    public static DocumentEntity toEntity(Document document) {
        DocumentEntity entity = new DocumentEntity();
        entity.setDocumentId(document.getDocumentId());
        entity.setOrganizationId(document.getOrganizationId());
        entity.setOwnerUserId(document.getOwnerUserId());
        entity.setTitle(document.getTitle());
        entity.setDocumentType(document.getDocumentType().name());
        entity.setCurrentStatus(document.getCurrentStatus().name());
        entity.setCreatedAt(document.getCreatedAt());
        entity.setUpdatedAt(document.getUpdatedAt());
        entity.setDeletedAt(document.getDeletedAt());
        return entity;
    }

    // DocumentVersionEntity → DocumentVersion 도메인
    public static DocumentVersion toDomain(DocumentVersionEntity entity) {
        return DocumentVersion.builder()
                .documentVersionId(entity.getDocumentVersionId())
                .documentId(entity.getDocumentId())
                .versionNo(entity.getVersionNo())
                .fileId(entity.getFileId())
                .fileHash(entity.getFileHash())
                .indexingStatus(IndexingStatus.valueOf(entity.getIndexingStatus()))
                .indexedChunkCount(entity.getIndexedChunkCount())
                .indexErrorMessage(entity.getIndexErrorMessage())
                .indexedAt(entity.getIndexedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // DocumentVersion 도메인 → DocumentVersionEntity
    public static DocumentVersionEntity toEntity(DocumentVersion version) {
        DocumentVersionEntity entity = new DocumentVersionEntity();
        entity.setDocumentVersionId(version.getDocumentVersionId());
        entity.setDocumentId(version.getDocumentId());
        entity.setVersionNo(version.getVersionNo());
        entity.setFileId(version.getFileId());
        entity.setFileHash(version.getFileHash());
        entity.setIndexingStatus(version.getIndexingStatus().name());
        entity.setIndexedChunkCount(version.getIndexedChunkCount());
        entity.setIndexErrorMessage(version.getIndexErrorMessage());
        entity.setIndexedAt(version.getIndexedAt());
        entity.setCreatedAt(version.getCreatedAt());
        return entity;
    }
}