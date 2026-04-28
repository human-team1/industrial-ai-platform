package com.example.factoryguard.domain.inspection.model;

import com.example.factoryguard.domain.inspection.vo.InspectionEventType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InspectionEventLog {

    private final Long eventId;
    private final Long inspectionId;
    private final InspectionEventType eventType;
    private final String message;
    private final LocalDateTime createdAt;
}
