package com.example.factoryguard.application.service.model;

import com.example.factoryguard.adapter.out.persistence.model.ModelDeploymentJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelVersionJpaEntity;
import com.example.factoryguard.application.dto.model.ResolvedModelDeployment;
import com.example.factoryguard.application.port.out.model.ModelManagementPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.DeploymentStatus;
import com.example.factoryguard.domain.model.vo.ModelDeployStatus;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import com.example.factoryguard.domain.model.vo.ModelUsagePurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActiveModelDeploymentResolver {

    private final ModelManagementPort modelManagementPort;

    public ResolvedModelDeployment resolve(Long organizationId, Long targetId, ModelUsagePurpose purpose) {
        ModelProfile requiredProfile = requiredProfile(purpose);
        if (targetId != null) {
            List<ResolvedModelDeployment> targetDeployments = findMatching(
                    organizationId, targetId, DeploymentScope.TARGET, requiredProfile);
            if (!targetDeployments.isEmpty()) {
                return single(targetDeployments);
            }
        }
        List<ResolvedModelDeployment> organizationDeployments = findMatching(
                organizationId, null, DeploymentScope.ORGANIZATION, requiredProfile);
        if (!organizationDeployments.isEmpty()) {
            return single(organizationDeployments);
        }
        throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT, "필요한 프로필의 활성 모델 배포가 없습니다.");
    }

    public ModelProfile requiredProfile(ModelUsagePurpose purpose) {
        if (purpose == ModelUsagePurpose.REALTIME_INSPECTION) {
            return ModelProfile.SPEED;
        }
        return ModelProfile.PERFORMANCE;
    }

    private List<ResolvedModelDeployment> findMatching(Long organizationId, Long targetId, DeploymentScope scope, ModelProfile profile) {
        return modelManagementPort.findActiveDeployments(organizationId, targetId, scope).stream()
                .filter(deployment -> deployment.getDeployStatus() == DeploymentStatus.DEPLOYED)
                .map(deployment -> toResolved(deployment, profile))
                .filter(Objects::nonNull)
                .toList();
    }

    private ResolvedModelDeployment toResolved(ModelDeploymentJpaEntity deployment, ModelProfile profile) {
        ModelVersionJpaEntity version = modelManagementPort.findModelVersionById(deployment.getModelVersionId())
                .orElse(null);
        if (version == null
                || version.getModelProfile() != profile
                || version.getDeployStatus() != ModelDeployStatus.DEPLOYED
                || !Boolean.TRUE.equals(version.getIsActive())) {
            return null;
        }
        return ResolvedModelDeployment.builder()
                .deployment(deployment)
                .version(version)
                .build();
    }

    private ResolvedModelDeployment single(List<ResolvedModelDeployment> deployments) {
        if (deployments.size() > 1) {
            throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT, "활성 모델 배포가 여러 개입니다. 검사대상 또는 모델 카테고리 배포 상태를 확인해주세요.");
        }
        return deployments.get(0);
    }
}
