package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.inspection.model.RunType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_run")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionRunJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspection_id")
    private Long inspectionId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_type", nullable = false)
    private RunType runType;

    @Column(name = "input_type")
    private String inputType;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_id")
    private String sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_status", nullable = false)
    private RunStatus runStatus;

    @Column(name = "applied_threshold", nullable = false)
    private double appliedThreshold;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public void updateStatus(RunStatus runStatus) {
        this.runStatus = runStatus;
        if (runStatus == RunStatus.COMPLETED || runStatus == RunStatus.FAILED || runStatus == RunStatus.STOPPED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public void updateError(String errorCode) {
        this.errorCode = errorCode;
    }

    @Builder
    public InspectionRunJpaEntity(Long organizationId, Long userId, Long targetId,
                                  RunType runType, String inputType, String sourceType,
                                  String sourceId, RunStatus runStatus, double appliedThreshold,
                                  String idempotencyKey, LocalDateTime startedAt) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.targetId = targetId;
        this.runType = runType;
        this.inputType = inputType;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.runStatus = runStatus;
        this.appliedThreshold = appliedThreshold;
        this.idempotencyKey = idempotencyKey;
        this.startedAt = startedAt;
    }
}