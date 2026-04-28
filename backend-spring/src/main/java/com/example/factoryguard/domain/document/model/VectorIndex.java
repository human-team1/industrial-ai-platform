package com.example.factoryguard.domain.document.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VectorIndex {

    private final Long vectorId;
    private final Long chunkId;
    private final String embeddingModel;
    private final String vectorRef;
    private final LocalDateTime createdAt;
}
