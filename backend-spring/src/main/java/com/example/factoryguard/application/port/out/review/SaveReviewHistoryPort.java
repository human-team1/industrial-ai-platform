package com.example.factoryguard.application.port.out.review;

import com.example.factoryguard.domain.review.model.ReviewHistory;

public interface SaveReviewHistoryPort {

    ReviewHistory save(ReviewHistory reviewHistory);
}
