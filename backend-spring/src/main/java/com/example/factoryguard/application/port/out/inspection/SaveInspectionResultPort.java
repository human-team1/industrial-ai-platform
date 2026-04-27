package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionResult;

public interface SaveInspectionResultPort {

    InspectionResult save(InspectionResult inspectionResult);
}