package com.example.factoryguard.application.dto.inspection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AiInspectionCommand {

    private final String fileUrl;
    private final double anomalyThreshold;
    private final double lowConfidenceThreshold;
}
