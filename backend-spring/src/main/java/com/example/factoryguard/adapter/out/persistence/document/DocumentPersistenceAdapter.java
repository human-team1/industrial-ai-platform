package com.example.factoryguard.adapter.out.persistence.document;

import com.example.factoryguard.adapter.out.persistence.document.entity.DocumentEntity;
import com.example.factoryguard.adapter.out.persistence.document.entity.DocumentVersionEntity;
import com.example.factoryguard.adapter.out.persistence.document.mapper.DocumentMapper;
import com.example.factoryguard.adapter.out.persistence.document.repository.DocumentJdbcRepository;
import com.example.factoryguard.application.port.out.document.SaveDocumentPort;
import com.example.factoryguard.application.port.out.document.SaveDocumentVersionPort;
import com.example.factoryguard.domain.document.model.Document;
import com.example.factoryguard.domain.document.model.DocumentVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DocumentPersistenceAdapter implements SaveDocumentPort, SaveDocumentVersionPort {

    private final DocumentJdbcRepository documentJdbcRepository;

    @Override
    public Document save(Document document) {
        DocumentEntity entity = DocumentMapper.toEntity(document);
        DocumentEntity savedEntity = documentJdbcRepository.saveDocument(entity);
        return DocumentMapper.toDomain(savedEntity);
    }

    @Override
    public DocumentVersion save(DocumentVersion documentVersion) {
        DocumentVersionEntity entity = DocumentMapper.toEntity(documentVersion);
        DocumentVersionEntity savedEntity = documentJdbcRepository.saveDocumentVersion(entity);
        return DocumentMapper.toDomain(savedEntity);
    }
}