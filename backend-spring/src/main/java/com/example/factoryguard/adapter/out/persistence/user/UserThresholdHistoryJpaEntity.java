package com.example.factoryguard.adapter.out.persistence.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_threshold_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserThresholdHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "threshold_history_id")
    private Long thresholdHistoryId;

    @Column(name = "threshold_id", nullable = false)
    private Long thresholdId;

    @Column(name = "version")
    private Integer version;

    @Column(name = "old_anomaly_threshold", nullable = false, columnDefinition = "DECIMAL(5,4)")
    private double oldAnomalyThreshold;

    @Column(name = "new_anomaly_threshold", nullable = false, columnDefinition = "DECIMAL(5,4)")
    private double newAnomalyThreshold;

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Builder
    public UserThresholdHistoryJpaEntity(Long thresholdId, Integer version,
                                         double oldAnomalyThreshold,
                                         double newAnomalyThreshold, String changeReason,
                                         Long changedBy) {
        this.thresholdId = thresholdId;
        this.version = version;
        this.oldAnomalyThreshold = oldAnomalyThreshold;
        this.newAnomalyThreshold = newAnomalyThreshold;
        this.changeReason = changeReason;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }
}