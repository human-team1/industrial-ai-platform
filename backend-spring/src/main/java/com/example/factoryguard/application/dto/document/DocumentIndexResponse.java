package com.example.factoryguard.application.dto.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DocumentIndexResponse {

    private String aiJobId;
    private Long indexJobId;
    private Long documentId;
    private Long documentVersionId;
    private Long organizationId;
    private Long fileId;
    private String fileKey;
    private String indexingStatus;
    private Integer indexedChunkCount;
    private Integer chunkCount;
    private Integer vectorCount;
    private String embeddingModel;
    private String collectionName;
    private LocalDateTime queuedAt;
    private LocalDateTime indexedAt;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<DocumentIndexedChunkResponse> chunks;
}
