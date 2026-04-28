package com.example.factoryguard.application.dto.document;

import com.example.factoryguard.domain.document.vo.DocumentType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateDocumentCommand {

    private final Long userId;
    private final Long organizationId;
    private final String title;
    private final DocumentType documentType;
    private final Long fileId;
}
