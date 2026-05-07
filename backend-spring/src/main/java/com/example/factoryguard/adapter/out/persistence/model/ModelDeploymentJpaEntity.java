package com.example.factoryguard.adapter.out.persistence.model;

import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.DeploymentStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "model_deployment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModelDeploymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deployment_id")
    private Long deploymentId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "model_version_id", nullable = false)
    private Long modelVersionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "deployment_scope", nullable = false)
    private DeploymentScope deploymentScope;

    @Enumerated(EnumType.STRING)
    @Column(name = "deploy_status", nullable = false)
    private DeploymentStatus deployStatus;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "deployed_at", nullable = false)
    private LocalDateTime deployedAt;

    @Column(name = "deployed_by")
    private Long deployedBy;

    @Column(name = "rollback_from_deployment_id")
    private Long rollbackFromDeploymentId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "rollback_flag", nullable = false)
    private Boolean rollbackFlag;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "delete_reason", columnDefinition = "TEXT")
    private String deleteReason;

    @Builder
    public ModelDeploymentJpaEntity(Long deploymentId, Long organizationId, Long targetId, Long modelVersionId,
                                    DeploymentScope deploymentScope, DeploymentStatus deployStatus, Boolean isActive,
                                    LocalDateTime deployedAt, Long deployedBy, Long rollbackFromDeploymentId,
                                    String reason, Boolean rollbackFlag, LocalDateTime deletedAt, Long deletedBy, String deleteReason) {
        this.deploymentId = deploymentId;
        this.organizationId = organizationId;
        this.targetId = targetId;
        this.modelVersionId = modelVersionId;
        this.deploymentScope = deploymentScope;
        this.deployStatus = deployStatus;
        this.isActive = isActive;
        this.deployedAt = deployedAt;
        this.deployedBy = deployedBy;
        this.rollbackFromDeploymentId = rollbackFromDeploymentId;
        this.reason = reason;
        this.rollbackFlag = rollbackFlag;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
        this.deleteReason = deleteReason;
    }

    public void deactivate(String reason) {
        this.isActive = false;
        this.deployStatus = DeploymentStatus.DEACTIVATED;
        if (reason != null && !reason.isBlank()) {
            this.reason = reason;
        }
    }

    public void activate(Long actorUserId, String reason) {
        if (this.deletedAt != null) {
            return;
        }
        this.isActive = true;
        this.deployStatus = DeploymentStatus.DEPLOYED;
        this.deployedAt = LocalDateTime.now();
        this.deployedBy = actorUserId;
        if (reason != null && !reason.isBlank()) {
            this.reason = reason;
        }
    }

    public void softDelete(Long actorUserId, String reason) {
        this.isActive = false;
        this.deployStatus = DeploymentStatus.DEACTIVATED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = actorUserId;
        this.deleteReason = reason;
        if (reason != null && !reason.isBlank()) {
            this.reason = reason;
        }
    }
}
