package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.domain.inspection.model.DecisionCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "inspection_result",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inspection_result_inspection_id",
                columnNames = "inspection_id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionResultJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "inspection_id", nullable = false)
    private Long inspectionId;

    @Column(name = "score", precision = 6, scale = 4)
    private Double score;

    @Column(name = "confidence", precision = 6, scale = 4)
    private Double confidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_code", length = 20)
    private DecisionCode decisionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_decision_code", length = 20)
    private DecisionCode finalDecisionCode;

    @Column(name = "result_status", length = 20)
    private String resultStatus;

    @Column(name = "threshold_source", length = 20)
    private String thresholdSource;

    @Column(name = "threshold_id")
    private Long thresholdId;

    @Column(name = "threshold_version")
    private Integer thresholdVersion;

    @Column(name = "model_version_id")
    private Long modelVersionId;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InspectionResultJpaEntity(Long inspectionId, Double score, Double confidence,
                                     DecisionCode decisionCode, DecisionCode finalDecisionCode,
                                     String resultStatus, String thresholdSource,
                                     Long thresholdId, Integer thresholdVersion,
                                     Long modelVersionId, String failureReason) {
        this.inspectionId = inspectionId;
        this.score = score;
        this.confidence = confidence;
        this.decisionCode = decisionCode;
        this.finalDecisionCode = finalDecisionCode;
        this.resultStatus = resultStatus;
        this.thresholdSource = thresholdSource;
        this.thresholdId = thresholdId;
        this.thresholdVersion = thresholdVersion;
        this.modelVersionId = modelVersionId;
        this.failureReason = failureReason;
    }
}