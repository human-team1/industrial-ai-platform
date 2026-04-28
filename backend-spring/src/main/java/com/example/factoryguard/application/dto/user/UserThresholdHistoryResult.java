package com.example.factoryguard.application.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserThresholdHistoryResult {

    private final Long thresholdHistoryId;
    private final Long thresholdId;
    private final double oldAnomalyThreshold;
    private final double newAnomalyThreshold;
    private final String changeReason;
    private final Long changedBy;
    private final LocalDateTime changedAt;
}
