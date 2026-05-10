package com.example.factoryguard.application.port.in.review;

import com.example.factoryguard.application.dto.review.ReviewHistoryResult;
import com.example.factoryguard.application.dto.review.ReviewInspectionResultCommand;

public interface ReviewInspectionResultUseCase {

    ReviewHistoryResult execute(ReviewInspectionResultCommand command);
}
