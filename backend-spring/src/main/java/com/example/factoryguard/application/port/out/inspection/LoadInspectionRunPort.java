package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionRun;

import java.util.List;
import java.util.Optional;

public interface LoadInspectionRunPort {

    Optional<InspectionRun> findById(Long inspectionId);

    List<InspectionRun> findAllByOrganizationId(Long organizationId);
}
