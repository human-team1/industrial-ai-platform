package com.example.factoryguard.domain.user.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserThreshold {

    private final Long thresholdId;
    private final Long userId;
    private final double anomalyThreshold;
    private final double lowConfidenceThreshold;
    private final double minAllowed;
    private final double maxAllowed;
    private final String applyScope;
    private final boolean isActive;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}