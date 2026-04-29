package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResultEventLogResponse {

    private final Long eventId;
    private final String eventType;
    private final String message;
    private final LocalDateTime createdAt;
}
