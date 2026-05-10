package com.example.factoryguard.application.dto.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class UpdateThresholdCommand {

    private final BigDecimal anomalyThreshold;
    private final BigDecimal lowConfidenceThreshold;
    private final String applyScope;
    private final String changeReason;
}
