package com.example.factoryguard.application.dto.inspection;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateUploadInspectionResult {

    private final Long inspectionId;
    private final Long resultId;
    private final String runStatus;
    private final String decisionCode;
}
