package com.example.factoryguard.application.dto.chat;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RagResultContext {

    private final Long resultId;
    private final Long inspectionId;
    private final String decisionCode;
    private final BigDecimal score;
    private final BigDecimal confidence;
    private final BigDecimal imageThreshold;
    private final String equipmentName;
    private final Long targetId;
    private final String anomalySummary;
    private final String modelVersion;
    private final String modelProfile;
}
