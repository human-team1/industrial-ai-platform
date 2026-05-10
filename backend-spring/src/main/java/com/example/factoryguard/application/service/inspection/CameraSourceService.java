package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.CreateCameraSourceCommand;
import com.example.factoryguard.application.dto.inspection.UpdateCameraSourceCommand;
import com.example.factoryguard.application.port.in.inspection.CreateCameraSourceUseCase;
import com.example.factoryguard.application.port.in.inspection.DeleteCameraSourceUseCase;
import com.example.factoryguard.application.port.in.inspection.GetCameraSourcesUseCase;
import com.example.factoryguard.application.port.in.inspection.UpdateCameraSourceUseCase;
import com.example.factoryguard.application.port.out.inspection.DeleteCameraSourcePort;
import com.example.factoryguard.application.port.out.inspection.LoadCameraSourcePort;
import com.example.factoryguard.application.port.out.inspection.SaveCameraSourcePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.inspection.model.CameraSource;
import com.example.factoryguard.domain.inspection.model.CameraStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CameraSourceService implements
        CreateCameraSourceUseCase,
        GetCameraSourcesUseCase,
        UpdateCameraSourceUseCase,
        DeleteCameraSourceUseCase {

    private final SaveCameraSourcePort saveCameraSourcePort;
    private final LoadCameraSourcePort loadCameraSourcePort;
    private final DeleteCameraSourcePort deleteCameraSourcePort;

    @Override
    public CameraSource execute(CreateCameraSourceCommand command) {
        validateStreamUrl(command.streamUrl());
        return saveCameraSourcePort.save(CameraSource.builder()
                .organizationId(command.organizationId())
                .userId(command.userId())
                .cameraName(command.cameraName())
                .streamUrl(command.streamUrl())
                .status(CameraStatus.ACTIVE)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CameraSource> execute(Long organizationId) {
        return loadCameraSourcePort.findByOrganizationId(organizationId);
    }

    @Override
    public CameraSource execute(UpdateCameraSourceCommand command) {
        CameraSource existing = loadCameraSourcePort.findById(command.cameraId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMERA_NOT_FOUND));
        if (!existing.getOrganizationId().equals(command.organizationId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (command.streamUrl() != null) {
            validateStreamUrl(command.streamUrl());
        }
        return saveCameraSourcePort.save(CameraSource.builder()
                .cameraId(existing.getCameraId())
                .organizationId(existing.getOrganizationId())
                .userId(existing.getUserId())
                .cameraName(command.cameraName() != null ? command.cameraName() : existing.getCameraName())
                .streamUrl(command.streamUrl() != null ? command.streamUrl() : existing.getStreamUrl())
                .status(command.status() != null ? command.status() : existing.getStatus())
                .build());
    }

    @Override
    public void execute(Long organizationId, Long cameraId) {
        CameraSource existing = loadCameraSourcePort.findById(cameraId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMERA_NOT_FOUND));
        if (!existing.getOrganizationId().equals(organizationId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        deleteCameraSourcePort.deleteById(cameraId);
    }

    private void validateStreamUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BusinessException(ErrorCode.CAMERA_INVALID_URL);
        }
        String lower = url.toLowerCase();
        if (!(lower.startsWith("rtsp://") || lower.startsWith("http://") || lower.startsWith("https://"))) {
            throw new BusinessException(ErrorCode.CAMERA_INVALID_URL);
        }
    }
}
