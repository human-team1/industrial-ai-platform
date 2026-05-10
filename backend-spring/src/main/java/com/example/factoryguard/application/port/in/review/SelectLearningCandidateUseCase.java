package com.example.factoryguard.application.port.in.review;

import com.example.factoryguard.application.dto.review.SelectLearningCandidateCommand;

public interface SelectLearningCandidateUseCase {

    void execute(SelectLearningCandidateCommand command);
}
