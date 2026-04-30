package com.example.factoryguard.adapter.out.persistence.review;

import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewQueueJpaRepository extends JpaRepository<ReviewQueueJpaEntity, Long> {

    Optional<ReviewQueueJpaEntity> findByResultId(Long resultId);

    List<ReviewQueueJpaEntity> findAllByQueueStatusOrderByQueuedAtAsc(ReviewQueueStatus queueStatus);
}
