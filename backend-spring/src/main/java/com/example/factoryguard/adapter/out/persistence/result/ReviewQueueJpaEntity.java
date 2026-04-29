package com.example.factoryguard.adapter.out.persistence.result;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_queue")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewQueueJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_queue_id")
    private Long reviewQueueId;

    @Column(name = "result_id", nullable = false)
    private Long resultId;

    @Column(name = "queue_status", nullable = false)
    private String queueStatus;

    @Column(name = "queued_reason")
    private String queuedReason;

    @Column(name = "queued_at")
    private LocalDateTime queuedAt;
}
