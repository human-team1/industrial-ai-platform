package com.example.factoryguard.domain.inspection.model;

import com.example.factoryguard.domain.review.vo.ReviewQueuedReason;

public final class DecisionCalculator {

    private DecisionCalculator() {}

    public static DecisionResult decide(double score, double confidence,
                                        double anomalyThreshold,
                                        double lowConfidenceThreshold,
                                        double boundaryMargin) {
        if (confidence < lowConfidenceThreshold) {
            return new DecisionResult(DecisionCode.RECHECK, ReviewQueuedReason.LOW_CONFIDENCE);
        }
        if (Math.abs(score - anomalyThreshold) <= boundaryMargin) {
            return new DecisionResult(DecisionCode.RECHECK, ReviewQueuedReason.BOUNDARY_SCORE);
        }
        if (score >= anomalyThreshold) {
            return new DecisionResult(DecisionCode.DEFECT, null);
        }
        return new DecisionResult(DecisionCode.NORMAL, null);
    }

    public record DecisionResult(DecisionCode decisionCode, ReviewQueuedReason queuedReason) {}
}
