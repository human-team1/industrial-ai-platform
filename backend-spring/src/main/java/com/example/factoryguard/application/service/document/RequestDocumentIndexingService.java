package com.example.factoryguard.application.service.document;

import com.example.factoryguard.application.dto.document.DocumentIndexRequest;
import com.example.factoryguard.application.dto.document.DocumentIndexResponse;
import com.example.factoryguard.application.dto.document.DocumentIndexingStatusResult;
import com.example.factoryguard.application.dto.document.DocumentIndexingTarget;
import com.example.factoryguard.application.dto.document.RequestDocumentIndexingCommand;
import com.example.factoryguard.application.port.in.document.RequestDocumentIndexingUseCase;
import com.example.factoryguard.application.port.out.document.CallDocumentIndexingPort;
import com.example.factoryguard.application.port.out.document.DocumentIndexPersistencePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.document.vo.DocumentIndexJobStatus;
import com.example.factoryguard.domain.document.vo.IndexingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RequestDocumentIndexingService implements RequestDocumentIndexingUseCase {

    private final DocumentIndexPersistencePort documentIndexPersistencePort;
    private final CallDocumentIndexingPort callDocumentIndexingPort;

    @Override
    public DocumentIndexingStatusResult execute(RequestDocumentIndexingCommand command) {
        DocumentIndexingTarget target = documentIndexPersistencePort.findIndexingTarget(command.getDocumentVersionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서 버전을 찾을 수 없습니다."));
        Long jobId = documentIndexPersistencePort.createIndexJob(
                target.getDocumentVersionId(),
                DocumentIndexJobStatus.PROCESSING,
                LocalDateTime.now()
        );
        documentIndexPersistencePort.markProcessing(target.getDocumentId(), target.getDocumentVersionId());

        try {
            DocumentIndexResponse indexResponse = callDocumentIndexingPort.index(
                    DocumentIndexRequest.builder()
                            .documentId(target.getDocumentId())
                            .documentVersionId(target.getDocumentVersionId())
                            .fileId(target.getFileId())
                            .fileKey(target.getFileKey())
                            .documentType(target.getDocumentType().name())
                            .organizationId(target.getOrganizationId())
                            .build(),
                    command.getRequestId()
            );
            int chunkCount = indexResponse.getChunkCount() == null ? 0 : indexResponse.getChunkCount();
            documentIndexPersistencePort.replaceChunksAndVectors(
                    target.getDocumentVersionId(),
                    indexResponse.getEmbeddingModel(),
                    indexResponse.getChunks()
            );
            documentIndexPersistencePort.markCompleted(
                    target.getDocumentId(),
                    target.getDocumentVersionId(),
                    jobId,
                    chunkCount,
                    indexResponse.getIndexedAt() == null ? LocalDateTime.now() : indexResponse.getIndexedAt()
            );
            return DocumentIndexingStatusResult.builder()
                    .documentVersionId(target.getDocumentVersionId())
                    .indexJobId(jobId)
                    .indexingStatus(IndexingStatus.COMPLETED)
                    .indexedChunkCount(chunkCount)
                    .jobStatus(DocumentIndexJobStatus.COMPLETED)
                    .build();
        } catch (BusinessException exception) {
            documentIndexPersistencePort.markFailed(
                    target.getDocumentId(),
                    target.getDocumentVersionId(),
                    jobId,
                    exception.getMessage()
            );
            throw exception;
        } catch (RuntimeException exception) {
            documentIndexPersistencePort.markFailed(
                    target.getDocumentId(),
                    target.getDocumentVersionId(),
                    jobId,
                    "문서 재인덱싱 중 오류가 발생했습니다."
            );
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR, "문서 재인덱싱 중 오류가 발생했습니다.");
        }
    }
}
