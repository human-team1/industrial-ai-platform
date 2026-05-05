package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DocumentIndexingTarget {

    private final Long documentId;
    private final Long documentVersionId;
    private final Long fileId;
    private final String fileKey;
    private final String fileName;
    private final String mimeType;
    private final String checksum;
    private final DocumentType documentType;
    private final Long organizationId;
    private final String title;
    private final String category;
    private final String equipmentType;
    private final List<String> tags;
}
