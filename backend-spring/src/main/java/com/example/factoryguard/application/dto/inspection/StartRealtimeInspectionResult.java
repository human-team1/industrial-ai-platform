package com.example.factoryguard.application.dto.inspection;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StartRealtimeInspectionResult {

    private final Long inspectionId;
    private final String runStatus;
}
