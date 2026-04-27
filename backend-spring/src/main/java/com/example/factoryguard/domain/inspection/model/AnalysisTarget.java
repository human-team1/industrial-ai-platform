package com.example.factoryguard.domain.inspection.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalysisTarget {

    private final Long targetId;
    private final Long organizationId;
    private final String targetName;
    private final String equipmentName;
    private final String productName;
}