package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class CreateDocumentWithFileCommand {

    private final Long userId;
    private final Long organizationId;
    private final MultipartFile file;
    private final String title;
    private final String category;
    private final String equipmentType;
    private final String description;
    private final String tags;
}
