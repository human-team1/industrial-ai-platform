package com.example.factoryguard.application.port.in.review;

import com.example.factoryguard.application.dto.review.RejectLearningCandidateCommand;

public interface RejectLearningCandidateUseCase {

    void execute(RejectLearningCandidateCommand command);
}
