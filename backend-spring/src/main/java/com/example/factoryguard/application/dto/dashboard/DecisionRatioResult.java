package com.example.factoryguard.application.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DecisionRatioResult {

    private final String decisionCode;
    private final Long count;
    private final BigDecimal ratio;
}
