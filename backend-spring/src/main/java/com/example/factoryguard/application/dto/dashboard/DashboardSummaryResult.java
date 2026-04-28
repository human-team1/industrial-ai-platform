package com.example.factoryguard.application.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummaryResult {

    private final Long inspectionCount;
    private final Long defectCount;
    private final Long reviewRequiredCount;
}
