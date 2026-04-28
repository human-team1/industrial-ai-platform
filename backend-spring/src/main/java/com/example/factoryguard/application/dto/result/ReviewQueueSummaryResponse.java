package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewQueueSummaryResponse {

    private final Boolean reviewRequired;
    private final String queueStatus;
    private final String queuedReason;
}
