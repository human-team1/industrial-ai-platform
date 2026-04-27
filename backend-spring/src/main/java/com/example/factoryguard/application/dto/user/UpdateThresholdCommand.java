package com.example.factoryguard.application.dto.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateThresholdCommand {

    private final double anomalyThreshold;
    private final double lowConfidenceThreshold;
    private final String applyScope;
    private final String changeReason;
}