package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.InspectionRunResult;

public interface GetInspectionRunUseCase {

    InspectionRunResult execute(Long inspectionId);
}
