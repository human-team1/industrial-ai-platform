package com.example.factoryguard.application.port.out.review;

import com.example.factoryguard.domain.review.model.ReviewQueue;

import java.util.List;
import java.util.Optional;

public interface LoadReviewQueuePort {

    Optional<ReviewQueue> findByResultId(Long resultId);

    List<ReviewQueue> findAllByStatus(String queueStatus);
}
