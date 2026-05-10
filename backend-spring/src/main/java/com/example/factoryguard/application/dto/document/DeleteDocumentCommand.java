package com.example.factoryguard.application.dto.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeleteDocumentCommand {

    private final Long userId;
    private final Long documentId;
}
