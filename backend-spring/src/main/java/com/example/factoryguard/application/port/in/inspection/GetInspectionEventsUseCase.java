package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionEventLog;

import java.util.List;

public interface GetInspectionEventsUseCase {
    List<InspectionEventLog> execute(Long organizationId, Long inspectionId);
}
