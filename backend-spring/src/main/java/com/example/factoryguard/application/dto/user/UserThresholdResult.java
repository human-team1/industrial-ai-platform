package com.example.factoryguard.application.dto.user;

import com.example.factoryguard.domain.user.model.UserThreshold;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserThresholdResult {

    private static final double DEFAULT_ANOMALY_THRESHOLD = 0.75;
    private static final double DEFAULT_LOW_CONFIDENCE_THRESHOLD = 0.55;
    private static final double DEFAULT_MIN_ALLOWED = 0.0;
    private static final double DEFAULT_MAX_ALLOWED = 1.0;

    private final Long thresholdId;
    private final BigDecimal anomalyThreshold;
    private final BigDecimal lowConfidenceThreshold;
    private final BigDecimal minAllowed;
    private final BigDecimal maxAllowed;
    private final String applyScope;
    private final Boolean isActive;
    private final LocalDateTime updatedAt;
    private final Integer thresholdVersion;

    public static UserThresholdResult fromUserThreshold(UserThreshold threshold, Integer thresholdVersion) {
        return UserThresholdResult.builder()
                .thresholdId(threshold.getThresholdId())
                .anomalyThreshold(BigDecimal.valueOf(threshold.getAnomalyThreshold()))
                .lowConfidenceThreshold(BigDecimal.valueOf(threshold.getLowConfidenceThreshold()))
                .minAllowed(BigDecimal.valueOf(threshold.getMinAllowed()))
                .maxAllowed(BigDecimal.valueOf(threshold.getMaxAllowed()))
                .applyScope(threshold.getApplyScope())
                .isActive(threshold.isActive())
                .updatedAt(threshold.getUpdatedAt())
                .thresholdVersion(thresholdVersion)
                .build();
    }

    public static UserThresholdResult systemDefault() {
        return UserThresholdResult.builder()
                .thresholdId(null)
                .anomalyThreshold(BigDecimal.valueOf(DEFAULT_ANOMALY_THRESHOLD))
                .lowConfidenceThreshold(BigDecimal.valueOf(DEFAULT_LOW_CONFIDENCE_THRESHOLD))
                .minAllowed(BigDecimal.valueOf(DEFAULT_MIN_ALLOWED))
                .maxAllowed(BigDecimal.valueOf(DEFAULT_MAX_ALLOWED))
                .applyScope("SYSTEM_DEFAULT")
                .isActive(true)
                .updatedAt(null)
                .thresholdVersion(null)
                .build();
    }
}
