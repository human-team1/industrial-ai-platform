package com.example.factoryguard.application.port.out.review;

import com.example.factoryguard.domain.review.model.LearningCandidate;

import java.util.List;
import java.util.Optional;

public interface LoadLearningCandidatePort {

    Optional<LearningCandidate> findById(Long learningCandidateId);

    List<LearningCandidate> findAllByStatus(String candidateStatus);
}
