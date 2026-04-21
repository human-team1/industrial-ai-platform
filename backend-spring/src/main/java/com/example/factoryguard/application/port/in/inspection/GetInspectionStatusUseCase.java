package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionStatus;

public interface GetInspectionStatusUseCase {

    InspectionStatus getStatus();
}
