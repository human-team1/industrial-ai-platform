package com.example.factoryguard.application.dto.user;

import com.example.factoryguard.domain.user.model.ThresholdSource;
import com.example.factoryguard.domain.user.model.UserThreshold;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserThresholdResult {

    private static final double DEFAULT_ANOMALY_THRESHOLD = 0.75;
    private static final double DEFAULT_LOW_CONFIDENCE_THRESHOLD = 0.55;

    private final double anomalyThreshold;
    private final double lowConfidenceThreshold;
    private final ThresholdSource source;

    public static UserThresholdResult fromUserThreshold(UserThreshold threshold) {
        return new UserThresholdResult(
                threshold.getAnomalyThreshold(),
                threshold.getLowConfidenceThreshold(),
                ThresholdSource.USER
        );
    }

    public static UserThresholdResult defaultThreshold() {
        return new UserThresholdResult(
                DEFAULT_ANOMALY_THRESHOLD,
                DEFAULT_LOW_CONFIDENCE_THRESHOLD,
                ThresholdSource.DEFAULT
        );
    }
}