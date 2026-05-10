package com.example.factoryguard.adapter.out.persistence.review;

import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import com.example.factoryguard.domain.review.vo.ReviewQueuedReason;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "review_queue",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_queue_result_id", columnNames = "result_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewQueueJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_queue_id")
    private Long reviewQueueId;

    @Column(name = "result_id", nullable = false)
    private Long resultId;

    @Enumerated(EnumType.STRING)
    @Column(name = "queue_status", nullable = false, length = 20)
    private ReviewQueueStatus queueStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "queued_reason", length = 50)
    private ReviewQueuedReason queuedReason;

    @CreationTimestamp
    @Column(name = "queued_at", nullable = false, updatable = false)
    private LocalDateTime queuedAt;

    public void updateStatus(ReviewQueueStatus queueStatus) {
        this.queueStatus = queueStatus;
    }

    @Builder
    public ReviewQueueJpaEntity(Long resultId, ReviewQueueStatus queueStatus,
                                ReviewQueuedReason queuedReason) {
        this.resultId = resultId;
        this.queueStatus = queueStatus;
        this.queuedReason = queuedReason;
    }
}
