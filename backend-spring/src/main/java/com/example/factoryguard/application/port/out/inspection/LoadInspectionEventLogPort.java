package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionEventLog;

import java.util.List;

public interface LoadInspectionEventLogPort {
    List<InspectionEventLog> findByInspectionIdOrderByCreatedAt(Long inspectionId);
}
