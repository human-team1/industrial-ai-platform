package com.example.factoryguard.application.port.in.review;

import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.dto.review.RerunReviewInspectionCommand;

public interface RerunReviewInspectionUseCase {
    SubmitInspectionResult execute(RerunReviewInspectionCommand command);
}
