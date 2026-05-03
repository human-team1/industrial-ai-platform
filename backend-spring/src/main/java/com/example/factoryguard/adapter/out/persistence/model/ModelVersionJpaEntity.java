package com.example.factoryguard.adapter.out.persistence.model;

import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelDeployStatus;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "model_version")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModelVersionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_version_id")
    private Long modelVersionId;

    @Column(name = "model_id", nullable = false)
    private Long modelId;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "version_name", nullable = false)
    private String versionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "model_category", nullable = false)
    private ModelCategory modelCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "model_profile", nullable = false)
    private ModelProfile modelProfile;

    @Column(name = "framework")
    private String framework;

    @Column(name = "input_size")
    private String inputSize;

    @Column(name = "threshold_default", precision = 5, scale = 4)
    private BigDecimal thresholdDefault;

    @Column(name = "accuracy", precision = 6, scale = 4)
    private BigDecimal accuracy;

    @Column(name = "precision_score", precision = 6, scale = 4)
    private BigDecimal precisionScore;

    @Column(name = "recall_score", precision = 6, scale = 4)
    private BigDecimal recallScore;

    @Column(name = "f1_score", precision = 6, scale = 4)
    private BigDecimal f1Score;

    @Column(name = "auroc_score", precision = 6, scale = 4)
    private BigDecimal aurocScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "deploy_status", nullable = false)
    private ModelDeployStatus deployStatus;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "validated_by")
    private Long validatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ModelVersionJpaEntity(Long modelVersionId, Long modelId, Long fileId, String versionName, ModelCategory modelCategory,
                                 ModelProfile modelProfile, String framework, String inputSize, BigDecimal thresholdDefault,
                                 BigDecimal accuracy, BigDecimal precisionScore, BigDecimal recallScore, BigDecimal f1Score,
                                 BigDecimal aurocScore, ModelDeployStatus deployStatus, Boolean isActive,
                                 LocalDateTime validatedAt, Long validatedBy) {
        this.modelVersionId = modelVersionId;
        this.modelId = modelId;
        this.fileId = fileId;
        this.versionName = versionName;
        this.modelCategory = modelCategory;
        this.modelProfile = modelProfile;
        this.framework = framework;
        this.inputSize = inputSize;
        this.thresholdDefault = thresholdDefault;
        this.accuracy = accuracy;
        this.precisionScore = precisionScore;
        this.recallScore = recallScore;
        this.f1Score = f1Score;
        this.aurocScore = aurocScore;
        this.deployStatus = deployStatus;
        this.isActive = isActive;
        this.validatedAt = validatedAt;
        this.validatedBy = validatedBy;
    }

    public void activate(LocalDateTime validatedAt, Long validatedBy) {
        this.deployStatus = ModelDeployStatus.VALIDATED;
        this.isActive = true;
        this.validatedAt = validatedAt;
        this.validatedBy = validatedBy;
    }

    public void deprecate() {
        this.deployStatus = ModelDeployStatus.DEPRECATED;
        this.isActive = false;
    }

    public void markDeployed() {
        this.deployStatus = ModelDeployStatus.DEPLOYED;
    }
}
