package com.example.factoryguard.application.dto.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RequestDocumentIndexingCommand {

    private final Long userId;
    private final Long documentVersionId;
    private final String requestId;
}
