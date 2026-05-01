package com.example.factoryguard.application.dto.user;

import com.example.factoryguard.domain.user.model.ThresholdSource;
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

    private final Long thresholdId;
    private final BigDecimal anomalyThreshold;
    private final BigDecimal lowConfidenceThreshold;
    private final BigDecimal minAllowed;
    private final BigDecimal maxAllowed;
    private final String applyScope;
    private final Boolean isActive;
    private final LocalDateTime updatedAt;
    private final Integer thresholdVersion;
    private final ThresholdSource source;

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
                .source(ThresholdSource.USER)
                .build();
    }

    public static UserThresholdResult defaultThreshold() {
        return UserThresholdResult.builder()
                .anomalyThreshold(BigDecimal.valueOf(DEFAULT_ANOMALY_THRESHOLD))
                .lowConfidenceThreshold(BigDecimal.valueOf(DEFAULT_LOW_CONFIDENCE_THRESHOLD))
                .source(ThresholdSource.SYSTEM_DEFAULT)
                .build();
    }
}
