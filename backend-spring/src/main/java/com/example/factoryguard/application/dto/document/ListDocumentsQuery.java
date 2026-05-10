package com.example.factoryguard.application.dto.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListDocumentsQuery {

    private final Long organizationId;
    private final Long ownerUserId;
    private final String documentType;
    private final String status;
    private final int page;
    private final int size;
}
