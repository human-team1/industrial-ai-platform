package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.AvailableInspectionModelItem;
import com.example.factoryguard.application.dto.inspection.AvailableRealtimeCameraItem;
import com.example.factoryguard.application.port.in.inspection.GetAvailableInspectionModelsUseCase;
import com.example.factoryguard.application.port.in.inspection.GetAvailableRealtimeCamerasUseCase;
import com.example.factoryguard.application.port.out.inspection.LoadCameraSourcePort;
import com.example.factoryguard.application.port.out.model.ModelManagementPort;
import com.example.factoryguard.domain.inspection.model.CameraStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionSelectionQueryService implements GetAvailableInspectionModelsUseCase, GetAvailableRealtimeCamerasUseCase {

    private final ModelManagementPort modelManagementPort;
    private final LoadCameraSourcePort loadCameraSourcePort;

    @Override
    @Transactional(readOnly = true)
    public List<AvailableInspectionModelItem> execute(Long organizationId, Long targetId, String inspectionType, String modelCategory) {
        Comparator<AvailableInspectionModelItem> comparator = Comparator
                .comparing((AvailableInspectionModelItem item) -> item.getTargetId() == null ? 1 : 0)
                .thenComparing(AvailableInspectionModelItem::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(AvailableInspectionModelItem::getModelProfile, Comparator.nullsLast(String::compareTo))
                .thenComparing(AvailableInspectionModelItem::getVersionName, Comparator.nullsLast(Comparator.reverseOrder()));

        return modelManagementPort.findAvailableInspectionModels(organizationId, targetId, modelCategory).stream()
                .map(item -> AvailableInspectionModelItem.builder()
                        .deploymentId(item.getDeploymentId())
                        .modelVersionId(item.getModelVersionId())
                        .modelId(item.getModelId())
                        .modelName(item.getModelName())
                        .versionName(item.getVersionName())
                        .displayName(buildDisplayName(item))
                        .modelCategory(item.getModelCategory())
                        .modelProfile(item.getModelProfile())
                        .deploymentScope(item.getDeploymentScope())
                        .organizationId(item.getOrganizationId())
                        .targetId(item.getTargetId())
                        .modelVersionStatus(item.getModelVersionStatus())
                        .deploymentStatus(item.getDeploymentStatus())
                        .isActive(item.getIsActive())
                        .thresholdDefault(item.getThresholdDefault())
                        .createdAt(item.getCreatedAt())
                        .build())
                .sorted(comparator)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableRealtimeCameraItem> execute(Long organizationId, Long targetId) {
        return loadCameraSourcePort.findByOrganizationId(organizationId).stream()
                .filter(camera -> camera.getStatus() == CameraStatus.ACTIVE)
                .sorted(Comparator.comparing(camera -> camera.getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                .map(camera -> AvailableRealtimeCameraItem.builder()
                        .cameraId(camera.getCameraId())
                        .cameraName(camera.getCameraName())
                        .organizationId(camera.getOrganizationId())
                        .targetId(targetId)
                        .targetName(null)
                        .status(camera.getStatus().name())
                        .displayName(camera.getCameraName())
                        .build())
                .toList();
    }

    private String buildDisplayName(AvailableInspectionModelItem item) {
        String scopeLabel = item.getTargetId() == null ? "조직 전체" : "검사대상 전용";
        return item.getModelName()
                + " / "
                + item.getModelCategory()
                + " / "
                + item.getModelProfile()
                + " / "
                + scopeLabel;
    }
}
