package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.AiInspectionResult;
import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.domain.inspection.model.DecisionCalculator;
import com.example.factoryguard.domain.inspection.model.DecisionCode;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.review.vo.ReviewQueuedReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InspectionDecisionEvaluator {

    private final DecisionProperties decisionProperties;

    public Outcome evaluate(AiInspectionResult result, InspectionInput input, ResolvedThreshold threshold) {
        boolean qualityGateEnabled = input.getQualityGateEnabled() == null
                ? true
                : input.getQualityGateEnabled();
        boolean qualityFailed = result.getQuality() != null
                && "FAILED".equalsIgnoreCase(result.getQuality().getStatus());
        if (qualityGateEnabled && qualityFailed) {
            return new Outcome(DecisionCode.RECHECK, ReviewQueuedReason.QUALITY_FAILED);
        }
        double score = result.getScore() == null ? 0.0d : result.getScore();
        double confidence = result.getConfidence();
        DecisionCalculator.DecisionResult dr = DecisionCalculator.decide(
                score, confidence,
                threshold.getAnomalyThreshold(),
                threshold.getLowConfidenceThreshold(),
                decisionProperties.getBoundaryMargin()
        );
        return new Outcome(dr.decisionCode(), dr.queuedReason());
    }

    public record Outcome(DecisionCode decisionCode, ReviewQueuedReason queuedReason) {}
}
