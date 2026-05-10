package com.example.factoryguard.application.port.out.review;

import com.example.factoryguard.domain.review.model.ReviewHistory;

import java.util.List;

public interface LoadReviewHistoryPort {

    List<ReviewHistory> findAllByResultId(Long resultId);
}
