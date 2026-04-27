package com.example.factoryguard.application.dto.inspection;

import com.example.factoryguard.domain.inspection.model.DecisionCode;
import com.example.factoryguard.domain.inspection.model.InspectionResult;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SubmitInspectionResult {

    private final Long inspectionId;
    private final Long resultId;
    private final double score;
    private final double confidence;
    private final DecisionCode decisionCode;
    private final String thresholdSource;

    public static SubmitInspectionResult of(InspectionRun run, InspectionResult result) {
        return new SubmitInspectionResult(
                run.getInspectionId(),
                result.getResultId(),
                result.getScore(),
                result.getConfidence(),
                result.getDecisionCode(),
                result.getThresholdSource()
        );
    }
}