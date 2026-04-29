package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResultInspectionResponse {

    private final String runType;
    private final String inputType;
    private final String sourceType;
    private final String runStatus;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
}
