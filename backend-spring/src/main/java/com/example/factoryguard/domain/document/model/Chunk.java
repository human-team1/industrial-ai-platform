package com.example.factoryguard.domain.document.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Chunk {

    private final Long chunkId;
    private final Long documentVersionId;
    private final Integer sequenceNo;
    private final String content;
    private final LocalDateTime createdAt;
}
