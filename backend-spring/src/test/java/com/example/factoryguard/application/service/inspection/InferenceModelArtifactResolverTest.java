package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.adapter.out.persistence.model.ModelDeploymentJpaEntity;
import com.example.factoryguard.application.port.out.file.LoadFilePort;
import com.example.factoryguard.application.port.out.model.ModelManagementPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.DeploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InferenceModelArtifactResolverTest {

    @Mock ModelManagementPort modelManagementPort;
    @Mock LoadFilePort loadFilePort;

    InferenceModelArtifactResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new InferenceModelArtifactResolver(modelManagementPort, loadFilePort);
    }

    @Test
    void nullOrganizationIdThrowsForbiddenInsteadOfInternalError() {
        when(modelManagementPort.findDeploymentById(700L)).thenReturn(Optional.of(
                ModelDeploymentJpaEntity.builder()
                        .deploymentId(700L)
                        .organizationId(1L)
                        .modelVersionId(50L)
                        .deploymentScope(DeploymentScope.ORGANIZATION)
                        .deployStatus(DeploymentStatus.DEPLOYED)
                        .isActive(true)
                        .deployedAt(LocalDateTime.now())
                        .build()
        ));

        assertThatThrownBy(() -> resolver.resolve(null, null, 700L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }
}
