package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionStatus;

public interface LoadInspectionStatusPort {

    InspectionStatus loadStatus();
}
