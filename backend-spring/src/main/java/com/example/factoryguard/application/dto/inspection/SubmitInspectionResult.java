package com.example.factoryguard.application.dto.inspection;

import com.example.factoryguard.domain.inspection.model.DecisionCode;
import com.example.factoryguard.domain.inspection.model.InspectionResult;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SubmitInspectionResult {

    private final Long inspectionId;
    private final RunStatus runStatus;
    private final ResultPayload result;

    public static SubmitInspectionResult of(InspectionRun run, InspectionResult result, boolean reviewQueued) {
        return new SubmitInspectionResult(
                run.getInspectionId(),
                run.getRunStatus(),
                new ResultPayload(
                        result.getResultId(),
                        result.getDecisionCode(),
                        result.getResultStatus(),
                        result.getScore(),
                        result.getConfidence(),
                        reviewQueued
                )
        );
    }

    public record ResultPayload(
            Long resultId,
            DecisionCode decisionCode,
            String resultStatus,
            Double score,
            Double confidence,
            boolean reviewQueued
    ) {}
}
