package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoadInspectionRunPort {
    Optional<InspectionRun> findRunById(Long inspectionId);
    List<InspectionRun> findRunsByOrganizationId(Long organizationId, int page, int size);
    List<InspectionRun> findRunsByStatusAndStartedAtBefore(RunStatus status, LocalDateTime threshold);
}
