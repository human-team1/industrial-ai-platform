package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionInput;

import java.util.List;

public interface LoadInspectionInputPort {
    List<InspectionInput> findByInspectionId(Long inspectionId);
}
