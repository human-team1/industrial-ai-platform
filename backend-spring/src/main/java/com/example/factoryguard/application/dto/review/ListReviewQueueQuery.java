package com.example.factoryguard.application.dto.review;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListReviewQueueQuery {

    private final Long organizationId;
    private final String queueStatus;
    private final int page;
    private final int size;
}
