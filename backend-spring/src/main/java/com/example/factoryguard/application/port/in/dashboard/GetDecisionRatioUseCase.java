package com.example.factoryguard.application.port.in.dashboard;

import com.example.factoryguard.application.dto.dashboard.DecisionRatioResult;

import java.util.List;

public interface GetDecisionRatioUseCase {

    List<DecisionRatioResult> execute(Long organizationId);
}
