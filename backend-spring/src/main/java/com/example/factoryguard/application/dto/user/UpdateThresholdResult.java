package com.example.factoryguard.application.dto.user;

import com.example.factoryguard.domain.user.model.UserThreshold;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateThresholdResult {

    private final Long thresholdId;
    private final double anomalyThreshold;
    private final double lowConfidenceThreshold;
    private final String applyScope;

    public static UpdateThresholdResult from(UserThreshold threshold) {
        return new UpdateThresholdResult(
                threshold.getThresholdId(),
                threshold.getAnomalyThreshold(),
                threshold.getLowConfidenceThreshold(),
                threshold.getApplyScope()
        );
    }
}