package com.example.factoryguard.application.port.in.review;

import com.example.factoryguard.application.dto.review.ListReviewQueueQuery;
import com.example.factoryguard.application.dto.review.ReviewQueueSummary;

import java.util.List;

public interface ListReviewQueueUseCase {

    List<ReviewQueueSummary> execute(ListReviewQueueQuery query);
}
