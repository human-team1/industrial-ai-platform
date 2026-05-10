package com.example.factoryguard.application.port.out.result;

import com.example.factoryguard.domain.inspection.model.InspectionResult;

import java.util.List;
import java.util.Optional;

public interface LoadInspectionResultPort {

    Optional<InspectionResult> findResultById(Long resultId);

    List<InspectionResult> findAllByInspectionId(Long inspectionId);
}
