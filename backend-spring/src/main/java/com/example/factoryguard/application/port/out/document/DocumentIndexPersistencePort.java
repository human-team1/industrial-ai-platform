package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.application.dto.document.DocumentIndexedChunkResponse;
import com.example.factoryguard.application.dto.document.DocumentIndexJobPollingTarget;
import com.example.factoryguard.application.dto.document.DocumentIndexingTarget;
import com.example.factoryguard.domain.document.vo.DocumentIndexJobStatus;
import com.example.factoryguard.domain.document.vo.IndexingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DocumentIndexPersistencePort {

    Long createIndexJob(Long documentVersionId, DocumentIndexJobStatus status, LocalDateTime startedAt);

    Optional<DocumentIndexingTarget> findIndexingTarget(Long documentVersionId);

    Optional<DocumentIndexingTarget> findLatestIndexingTargetByDocumentId(Long documentId);

    List<DocumentIndexJobPollingTarget> findProcessingJobs(int limit);

    void markProcessing(Long documentId, Long documentVersionId);

    void markEnqueued(Long documentId, Long documentVersionId, Long jobId, String aiJobId);

    void replaceChunksAndVectors(
            Long documentVersionId,
            String embeddingModel,
            List<DocumentIndexedChunkResponse> chunks
    );

    void markCompleted(
            Long documentId,
            Long documentVersionId,
            Long jobId,
            int chunkCount,
            LocalDateTime indexedAt
    );

    void markFailed(Long documentId, Long documentVersionId, Long jobId, String errorMessage);

    void recordDeindexFailure(Long documentVersionId, String errorMessage);
}
