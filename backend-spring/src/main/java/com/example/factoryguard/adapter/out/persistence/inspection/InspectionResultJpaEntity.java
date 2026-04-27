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
@Table(name = "inspection_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionResultJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "inspection_id", nullable = false)
    private Long inspectionId;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_code", nullable = false)
    private DecisionCode decisionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_decision_code")
    private DecisionCode finalDecisionCode;

    @Column(name = "result_status")
    private String resultStatus;

    @Column(name = "threshold_source")
    private String thresholdSource;

    @Column(name = "threshold_id")
    private Long thresholdId;

    @Column(name = "threshold_version")
    private String thresholdVersion;

    @Column(name = "model_version_id")
    private Long modelVersionId;

    @Column(name = "failure_reason")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InspectionResultJpaEntity(Long inspectionId, double score, double confidence,
                                     DecisionCode decisionCode, DecisionCode finalDecisionCode,
                                     String resultStatus, String thresholdSource,
                                     Long thresholdId, String thresholdVersion,
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