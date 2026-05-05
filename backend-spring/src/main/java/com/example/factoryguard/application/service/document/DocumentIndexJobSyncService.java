package com.example.factoryguard.application.service.document;

import com.example.factoryguard.application.dto.document.DocumentIndexJobPollingTarget;
import com.example.factoryguard.application.dto.document.DocumentIndexResponse;
import com.example.factoryguard.application.port.out.document.DocumentIndexPersistencePort;
import com.example.factoryguard.application.port.out.document.DocumentIndexingAiPort;
import com.example.factoryguard.config.client.AiServerProperties;
import com.example.factoryguard.domain.document.vo.DocumentIndexJobStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentIndexJobSyncService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIndexJobSyncService.class);

    private final DocumentIndexPersistencePort documentIndexPersistencePort;
    private final DocumentIndexingAiPort documentIndexingAiPort;
    private final AiServerProperties aiServerProperties;

    @Scheduled(fixedDelayString = "${app.ai-server.document-index.poll-interval-ms:10000}")
    public void pollProcessingJobs() {
        if (!aiServerProperties.getDocumentIndex().isPollingEnabled()) {
            return;
        }
        List<DocumentIndexJobPollingTarget> jobs = documentIndexPersistencePort.findProcessingJobs(
                aiServerProperties.getDocumentIndex().getPollBatchSize());
        for (DocumentIndexJobPollingTarget job : jobs) {
            try {
                sync(job);
            } catch (RuntimeException exception) {
                log.warn("Document index status sync failed, indexJobId={}, aiJobId={}, documentVersionId={}",
                        job.getIndexJobId(), job.getAiJobId(), job.getDocumentVersionId());
            }
        }
    }

    @Transactional
    public void sync(DocumentIndexJobPollingTarget job) {
        DocumentIndexResponse response = documentIndexingAiPort.getStatus(job.getAiJobId());
        DocumentIndexJobStatus status = parseStatus(response.getIndexingStatus());
        if (status == DocumentIndexJobStatus.COMPLETED) {
            documentIndexPersistencePort.replaceChunksAndVectors(
                    job.getDocumentVersionId(),
                    response.getEmbeddingModel(),
                    response.getChunks()
            );
            documentIndexPersistencePort.markCompleted(
                    job.getDocumentId(),
                    job.getDocumentVersionId(),
                    job.getIndexJobId(),
                    response.getIndexedChunkCount() == null ? 0 : response.getIndexedChunkCount(),
                    response.getCompletedAt() == null ? LocalDateTime.now() : response.getCompletedAt()
            );
            return;
        }
        if (status == DocumentIndexJobStatus.FAILED) {
            documentIndexPersistencePort.markFailed(
                    job.getDocumentId(),
                    job.getDocumentVersionId(),
                    job.getIndexJobId(),
                    response.getErrorMessage()
            );
        }
    }

    private DocumentIndexJobStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return DocumentIndexJobStatus.PROCESSING;
        }
        return DocumentIndexJobStatus.valueOf(value);
    }
}
