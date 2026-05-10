package com.example.factoryguard.adapter.out.persistence.review;

import com.example.factoryguard.application.port.out.review.LoadReviewQueuePort;
import com.example.factoryguard.application.port.out.review.SaveReviewQueuePort;
import com.example.factoryguard.domain.review.model.ReviewQueue;
import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewQueuePersistenceAdapter implements SaveReviewQueuePort, LoadReviewQueuePort {

    private final ReviewQueueJpaRepository reviewQueueJpaRepository;

    @Override
    public ReviewQueue save(ReviewQueue reviewQueue) {
        ReviewQueueJpaEntity saved;
        if (reviewQueue.getReviewQueueId() != null) {
            ReviewQueueJpaEntity existing = reviewQueueJpaRepository.findById(reviewQueue.getReviewQueueId())
                    .orElseThrow();
            existing.updateStatus(reviewQueue.getQueueStatus());
            saved = existing;
        } else {
            saved = reviewQueueJpaRepository.save(
                    ReviewQueueJpaEntity.builder()
                            .resultId(reviewQueue.getResultId())
                            .queueStatus(reviewQueue.getQueueStatus())
                            .queuedReason(reviewQueue.getQueuedReason())
                            .build()
            );
        }
        return toDomain(saved);
    }

    @Override
    public Optional<ReviewQueue> findByResultId(Long resultId) {
        return reviewQueueJpaRepository.findByResultId(resultId).map(this::toDomain);
    }

    @Override
    public Optional<ReviewQueue> findById(Long reviewQueueId) {
        return reviewQueueJpaRepository.findById(reviewQueueId).map(this::toDomain);
    }

    @Override
    public List<ReviewQueue> findAllByStatus(ReviewQueueStatus queueStatus) {
        return reviewQueueJpaRepository.findAllByQueueStatusOrderByQueuedAtAsc(queueStatus).stream()
                .map(this::toDomain)
                .toList();
    }

    private ReviewQueue toDomain(ReviewQueueJpaEntity e) {
        return ReviewQueue.builder()
                .reviewQueueId(e.getReviewQueueId())
                .resultId(e.getResultId())
                .queueStatus(e.getQueueStatus())
                .queuedReason(e.getQueuedReason())
                .queuedAt(e.getQueuedAt())
                .build();
    }
}
