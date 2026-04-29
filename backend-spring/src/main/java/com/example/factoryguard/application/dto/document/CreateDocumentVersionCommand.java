package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class CreateDocumentVersionCommand {

    private final Long userId;
    private final Long organizationId;
    private final Long documentId;
    private final MultipartFile file;
    private final String changeReason;
}
