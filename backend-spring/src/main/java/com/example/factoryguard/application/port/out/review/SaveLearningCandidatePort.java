package com.example.factoryguard.application.port.out.review;

import com.example.factoryguard.domain.review.model.LearningCandidate;

public interface SaveLearningCandidatePort {

    LearningCandidate save(LearningCandidate learningCandidate);
}
