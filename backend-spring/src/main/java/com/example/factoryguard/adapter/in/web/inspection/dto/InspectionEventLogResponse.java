package com.example.factoryguard.adapter.in.web.inspection.dto;

import com.example.factoryguard.domain.inspection.model.InspectionEventLog;
import com.example.factoryguard.domain.inspection.model.InspectionEventType;

import java.time.LocalDateTime;

public record InspectionEventLogResponse(
        Long eventId,
        Long inspectionId,
        InspectionEventType eventType,
        String message,
        LocalDateTime createdAt
) {
    public static InspectionEventLogResponse from(InspectionEventLog e) {
        return new InspectionEventLogResponse(
                e.getEventId(),
                e.getInspectionId(),
                e.getEventType(),
                e.getMessage(),
                e.getCreatedAt()
        );
    }
}
