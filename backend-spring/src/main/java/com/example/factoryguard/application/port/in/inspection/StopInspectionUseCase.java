package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionRun;

public interface StopInspectionUseCase {
    InspectionRun execute(Long userId, Long organizationId, Long inspectionId);
}
