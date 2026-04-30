package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionEventLog;

public interface SaveInspectionEventLogPort {
    InspectionEventLog save(InspectionEventLog log);
}
