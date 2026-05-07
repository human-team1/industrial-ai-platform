package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.AvailableAnalysisTargetItem;

import java.util.List;

public interface GetAvailableAnalysisTargetsUseCase {

    List<AvailableAnalysisTargetItem> execute(Long organizationId);
}
