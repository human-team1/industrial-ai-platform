package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.domain.inspection.model.DecisionCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
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

    @Column(name = "score", precision = 8, scale = 4)
    private BigDecimal score;

    @Column(name = "confidence", precision = 6, scale = 4)
    private BigDecimal confidence;

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

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "category_type", length = 50)
    private String categoryType;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "model_profile", length = 20)
    private String modelProfile;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "anomaly_score", precision = 12, scale = 6)
    private BigDecimal anomalyScore;

    @Column(name = "image_threshold", precision = 12, scale = 6)
    private BigDecimal imageThreshold;

    @Column(name = "predicted_label", length = 50)
    private String predictedLabel;

    @Column(name = "heatmap_path", length = 500)
    private String heatmapPath;

    @Column(name = "pixel_threshold", precision = 12, scale = 6)
    private BigDecimal pixelThreshold;

    @Column(name = "inference_time")
    private Long inferenceTime;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InspectionResultJpaEntity(Long inspectionId, BigDecimal score, BigDecimal confidence,
                                     DecisionCode decisionCode, DecisionCode finalDecisionCode,
                                     String resultStatus, String thresholdSource,
                                     Long thresholdId, Integer thresholdVersion,
                                     Long modelVersionId, String imagePath, String categoryType,
                                     String category, String modelProfile, String modelName,
                                     BigDecimal anomalyScore, BigDecimal imageThreshold,
                                     String predictedLabel, String heatmapPath,
                                     BigDecimal pixelThreshold, Long inferenceTime,
                                     String failureReason) {
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
        this.imagePath = imagePath;
        this.categoryType = categoryType;
        this.category = category;
        this.modelProfile = modelProfile;
        this.modelName = modelName;
        this.anomalyScore = anomalyScore;
        this.imageThreshold = imageThreshold;
        this.predictedLabel = predictedLabel;
        this.heatmapPath = heatmapPath;
        this.pixelThreshold = pixelThreshold;
        this.inferenceTime = inferenceTime;
        this.failureReason = failureReason;
    }
}
