package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DocumentSearchQuery {

    private final String keyword;
    private final String documentType;
    private final String indexingStatus;
    private final String category;
    private final String equipmentType;
    private final String author;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int page;
    private final int size;
}
