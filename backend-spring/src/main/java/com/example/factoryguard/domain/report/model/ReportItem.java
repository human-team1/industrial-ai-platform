package com.example.factoryguard.domain.report.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportItem {

    private final Long reportItemId;
    private final Long reportId;
    private final Long resultId;
    private final String summaryText;
}
