package com.example.factoryguard.application.port.in.review;

import com.example.factoryguard.application.dto.review.ReviewTargetDetail;

public interface GetReviewTargetUseCase {

    ReviewTargetDetail execute(Long resultId);
}
