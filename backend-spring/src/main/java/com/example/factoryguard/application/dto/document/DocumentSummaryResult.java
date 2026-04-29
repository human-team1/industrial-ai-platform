package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentSummaryResult {

    private final long totalCount;
    private final long pdfCount;
    private final long docxCount;
    private final long completedCount;
    private final long processingCount;
    private final long failedCount;
}
