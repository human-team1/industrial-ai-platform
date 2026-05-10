package com.example.factoryguard.application.port.out.review;

import com.example.factoryguard.domain.review.model.ReviewQueue;

public interface SaveReviewQueuePort {

    ReviewQueue save(ReviewQueue reviewQueue);
}
