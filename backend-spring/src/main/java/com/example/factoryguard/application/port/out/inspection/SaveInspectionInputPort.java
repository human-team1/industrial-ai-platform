package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionInput;

public interface SaveInspectionInputPort {
    InspectionInput save(InspectionInput input);
}
