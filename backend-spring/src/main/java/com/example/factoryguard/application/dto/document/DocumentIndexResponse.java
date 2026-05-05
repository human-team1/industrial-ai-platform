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

    private Long documentId;
    private Long documentVersionId;
    private Long fileId;
    private String fileKey;
    private String indexingStatus;
    private Integer chunkCount;
    private Integer vectorCount;
    private String embeddingModel;
    private String collectionName;
    private LocalDateTime indexedAt;
    private List<DocumentIndexedChunkResponse> chunks;
}
