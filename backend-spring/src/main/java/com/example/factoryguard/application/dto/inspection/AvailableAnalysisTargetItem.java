package com.example.factoryguard.application.dto.inspection;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailableAnalysisTargetItem {

    private final Long targetId;
    private final String targetName;
    private final String equipmentName;
    private final String productName;
    private final String displayName;
}
