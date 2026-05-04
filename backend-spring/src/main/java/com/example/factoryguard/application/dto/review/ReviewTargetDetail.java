package com.example.factoryguard.application.dto.review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewTargetDetail {

    private final Long reviewQueueId;
    private final Long resultId;
    private final Long inspectionId;
    private final String decisionCode;
    private final String finalDecisionCode;
    private final String failureReason;
    private final ReviewModelInfo model;
}
