package com.example.factoryguard.application.service.document;

import com.example.factoryguard.application.dto.document.CreateDocumentCommand;
import com.example.factoryguard.application.dto.document.DocumentDetail;
import com.example.factoryguard.application.port.in.document.CreateDocumentUseCase;
import com.example.factoryguard.application.port.out.document.SaveDocumentPort;
import com.example.factoryguard.application.port.out.document.SaveDocumentVersionPort;
import com.example.factoryguard.application.port.out.document.SaveDocumentIndexJobPort;
import com.example.factoryguard.application.port.out.rag.RequestDocumentIndexingPort;
import com.example.factoryguard.domain.document.model.Document;
import com.example.factoryguard.domain.document.model.DocumentVersion;
import com.example.factoryguard.domain.document.model.DocumentIndexJob;
import com.example.factoryguard.domain.document.vo.DocumentStatus;
import com.example.factoryguard.domain.document.vo.DocumentIndexJobStatus;
import com.example.factoryguard.domain.document.vo.IndexingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateDocumentService implements CreateDocumentUseCase {

    private final SaveDocumentPort saveDocumentPort;
    private final SaveDocumentVersionPort saveDocumentVersionPort;
    private final SaveDocumentIndexJobPort saveDocumentIndexJobPort;
    private final RequestDocumentIndexingPort requestDocumentIndexingPort;

    @Override
    public DocumentDetail execute(CreateDocumentCommand command) {
        // 1. 문서 생성
        Document document = Document.builder()
                .organizationId(command.getOrganizationId())
                .ownerUserId(command.getUserId())
                .title(command.getTitle())
                .documentType(command.getDocumentType())
                .currentStatus(DocumentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Document savedDocument = saveDocumentPort.save(document);

        // 2. 문서 버전 생성
        DocumentVersion documentVersion = DocumentVersion.builder()
                .documentId(savedDocument.getDocumentId())
                .versionNo(1)
                .fileId(command.getFileId())
                .indexingStatus(IndexingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        DocumentVersion savedVersion = saveDocumentVersionPort.save(documentVersion);

        // 3. 인덱싱 작업 생성
        DocumentIndexJob indexJob = DocumentIndexJob.builder()
                .documentVersionId(savedVersion.getDocumentVersionId())
                .jobStatus(DocumentIndexJobStatus.PENDING)
                .build();

        saveDocumentIndexJobPort.save(indexJob);

        // 4. FastAPI 인덱싱 요청
        requestDocumentIndexingPort.request(savedVersion.getDocumentVersionId());

        // 5. 결과 반환
        return DocumentDetail.builder()
                .documentId(savedDocument.getDocumentId())
                .organizationId(savedDocument.getOrganizationId())
                .ownerUserId(savedDocument.getOwnerUserId())
                .title(savedDocument.getTitle())
                .documentType(savedDocument.getDocumentType())
                .currentStatus(savedDocument.getCurrentStatus())
                .build();
    }
}