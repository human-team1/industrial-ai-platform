package com.example.factoryguard.application.dto.review;

import com.example.factoryguard.domain.review.vo.ReviewQueuedReason;
import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewQueueSummary {

    private final Long reviewQueueId;
    private final Long resultId;
    private final ReviewQueueStatus queueStatus;
    private final ReviewQueuedReason queuedReason;
    private final LocalDateTime queuedAt;
    private final Long modelVersionId;
    private final String modelCategory;
    private final String modelProfile;
}
