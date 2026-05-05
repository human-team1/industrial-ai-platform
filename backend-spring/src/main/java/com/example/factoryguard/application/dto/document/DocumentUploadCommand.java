package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DocumentUploadCommand {

    private final Long userId;
    private final Long organizationId;
    private final String title;
    private final DocumentType documentType;
    private final String category;
    private final String equipmentType;
    private final String description;
    private final List<String> tags;
    private final String originalFileName;
    private final String mimeType;
    private final byte[] content;
    private final String requestId;
}
