package com.example.factoryguard.application.port.in.review;

import com.example.factoryguard.application.dto.review.ReviewHistoryResult;

import java.util.List;

public interface ListReviewHistoryUseCase {

    List<ReviewHistoryResult> execute(Long resultId);
}
