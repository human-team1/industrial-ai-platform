package com.example.factoryguard.adapter.out.persistence.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_threshold")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserThresholdJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "threshold_id")
    private Long thresholdId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "anomaly_threshold", nullable = false, columnDefinition = "DECIMAL(8,4)")
    private BigDecimal anomalyThreshold;

    @Column(name = "low_confidence_threshold", nullable = false)
    private double lowConfidenceThreshold;

    @Column(name = "min_allowed", nullable = false, columnDefinition = "DECIMAL(8,4)")
    private BigDecimal minAllowed;

    @Column(name = "max_allowed", nullable = false, columnDefinition = "DECIMAL(8,4)")
    private BigDecimal maxAllowed;

    @Column(name = "apply_scope")
    private String applyScope;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void update(double anomalyThreshold, double lowConfidenceThreshold, String applyScope) {
        this.anomalyThreshold = BigDecimal.valueOf(anomalyThreshold);
        this.lowConfidenceThreshold = lowConfidenceThreshold;
        this.applyScope = applyScope;
    }

    public double getAnomalyThresholdAsDouble() {
        return anomalyThreshold == null ? 0.0 : anomalyThreshold.doubleValue();
    }

    public double getMinAllowedAsDouble() {
        return minAllowed == null ? 0.0 : minAllowed.doubleValue();
    }

    public double getMaxAllowedAsDouble() {
        return maxAllowed == null ? Double.MAX_VALUE : maxAllowed.doubleValue();
    }

    @Builder
    public UserThresholdJpaEntity(Long userId, double anomalyThreshold, double lowConfidenceThreshold,
                                  double minAllowed, double maxAllowed, String applyScope, boolean isActive) {
        this.userId = userId;
        this.anomalyThreshold = BigDecimal.valueOf(anomalyThreshold);
        this.lowConfidenceThreshold = lowConfidenceThreshold;
        this.minAllowed = BigDecimal.valueOf(minAllowed);
        this.maxAllowed = BigDecimal.valueOf(maxAllowed);
        this.applyScope = applyScope;
        this.isActive = isActive;
    }
}