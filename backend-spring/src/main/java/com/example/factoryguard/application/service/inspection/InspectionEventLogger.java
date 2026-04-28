package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.out.inspection.SaveInspectionEventLogPort;
import com.example.factoryguard.domain.inspection.model.InspectionEventLog;
import com.example.factoryguard.domain.inspection.model.InspectionEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InspectionEventLogger {

    private final SaveInspectionEventLogPort saveInspectionEventLogPort;

    @Transactional(propagation = Propagation.REQUIRED)
    public void log(Long inspectionId, InspectionEventType type, String message) {
        saveInspectionEventLogPort.save(InspectionEventLog.builder()
                .inspectionId(inspectionId)
                .eventType(type)
                .message(message)
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(Long inspectionId, InspectionEventType type, String message) {
        saveInspectionEventLogPort.save(InspectionEventLog.builder()
                .inspectionId(inspectionId)
                .eventType(type)
                .message(message)
                .build());
    }
}
