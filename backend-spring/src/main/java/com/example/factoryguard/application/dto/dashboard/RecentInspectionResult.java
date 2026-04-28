package com.example.factoryguard.application.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecentInspectionResult {

    private final Long resultId;
    private final Long inspectionId;
    private final String targetName;
    private final String decisionCode;
    private final LocalDateTime createdAt;
}
