package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionInput;

import java.util.Optional;

public interface LoadInspectionInputPort {

    Optional<InspectionInput> findByInspectionId(Long inspectionId);
}
