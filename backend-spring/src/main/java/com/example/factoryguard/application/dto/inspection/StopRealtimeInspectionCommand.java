package com.example.factoryguard.application.dto.inspection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StopRealtimeInspectionCommand {

    private final Long userId;
    private final String sessionId;
    private final Long inspectionId;
}
