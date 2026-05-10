package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DocumentIndexRequest {

    private final Long indexJobId;
    private final Long documentId;
    private final Long documentVersionId;
    private final Long organizationId;
    private final Long fileId;
    private final String fileKey;
    private final String fileName;
    private final String mimeType;
    private final String checksum;
    private final String title;
    private final String documentType;
    private final String category;
    private final String equipmentType;
    private final List<String> tags;
    private final Integer chunkSize;
    private final Integer chunkOverlap;
    private final String embeddingModel;
}
