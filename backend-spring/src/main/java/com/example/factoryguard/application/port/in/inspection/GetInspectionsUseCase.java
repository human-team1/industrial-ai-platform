package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionRun;

import java.util.List;

public interface GetInspectionsUseCase {
    List<InspectionRun> list(Long organizationId, int page, int size);
    InspectionRun detail(Long organizationId, Long inspectionId);
}
