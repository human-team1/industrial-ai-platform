package com.example.factoryguard.application.dto.chat;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RagAnswerSource {

    private final Long sourceId;
    private final Long documentId;
    private final String documentTitle;
    private final String documentType;
    private final Long chunkId;
    private final String vectorRef;
    private final Integer page;
    private final String section;
    private final String sourceSnippet;
    private final BigDecimal score;
}
