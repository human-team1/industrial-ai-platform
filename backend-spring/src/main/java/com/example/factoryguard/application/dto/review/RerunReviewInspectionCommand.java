package com.example.factoryguard.application.dto.review;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RerunReviewInspectionCommand {

    private final Long userId;
    private final String sessionId;
    private final Long reviewQueueId;
    private final Long deploymentId;
}
