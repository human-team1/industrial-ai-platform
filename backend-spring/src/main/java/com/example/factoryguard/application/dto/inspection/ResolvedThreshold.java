package com.example.factoryguard.application.dto.inspection;

import com.example.factoryguard.domain.user.model.ThresholdSource;
import com.example.factoryguard.domain.user.model.UserThreshold;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResolvedThreshold {

    private static final double DEFAULT_ANOMALY_THRESHOLD = 0.75;
    private static final double DEFAULT_LOW_CONFIDENCE_THRESHOLD = 0.55;

    private final double anomalyThreshold;
    private final double lowConfidenceThreshold;
    private final ThresholdSource source;
    private final Long thresholdId;
    private final Integer thresholdVersion;

    public static ResolvedThreshold fromUserThreshold(UserThreshold threshold, Integer version) {
        return new ResolvedThreshold(
                threshold.getAnomalyThreshold(),
                threshold.getLowConfidenceThreshold(),
                ThresholdSource.USER,
                threshold.getThresholdId(),
                version
        );
    }

    public static ResolvedThreshold defaultThreshold() {
        return new ResolvedThreshold(
                DEFAULT_ANOMALY_THRESHOLD,
                DEFAULT_LOW_CONFIDENCE_THRESHOLD,
                ThresholdSource.SYSTEM_DEFAULT,
                null,
                null
        );
    }
}