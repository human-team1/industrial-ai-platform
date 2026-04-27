package com.example.factoryguard.adapter.in.web.user.dto;

import com.example.factoryguard.application.dto.user.UpdateThresholdCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateThresholdRequest {

    private double anomalyThreshold;
    private double lowConfidenceThreshold;
    private String applyScope;
    private String changeReason;

    public UpdateThresholdCommand toCommand() {
        return new UpdateThresholdCommand(anomalyThreshold, lowConfidenceThreshold, applyScope, changeReason);
    }
}