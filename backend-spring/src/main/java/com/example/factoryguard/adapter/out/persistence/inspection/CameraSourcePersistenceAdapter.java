package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.application.port.out.inspection.DeleteCameraSourcePort;
import com.example.factoryguard.application.port.out.inspection.LoadCameraSourcePort;
import com.example.factoryguard.application.port.out.inspection.SaveCameraSourcePort;
import com.example.factoryguard.domain.inspection.model.CameraSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CameraSourcePersistenceAdapter implements
        SaveCameraSourcePort,
        LoadCameraSourcePort,
        DeleteCameraSourcePort {

    private final CameraSourceJpaRepository repository;

    @Override
    public CameraSource save(CameraSource camera) {
        CameraSourceJpaEntity saved;
        if (camera.getCameraId() != null) {
            CameraSourceJpaEntity existing = repository.findById(camera.getCameraId()).orElseThrow();
            existing.update(camera.getCameraName(), camera.getStreamUrl(), camera.getStatus());
            saved = existing;
        } else {
            saved = repository.save(CameraSourceJpaEntity.builder()
                    .organizationId(camera.getOrganizationId())
                    .userId(camera.getUserId())
                    .cameraName(camera.getCameraName())
                    .streamUrl(camera.getStreamUrl())
                    .status(camera.getStatus())
                    .build());
        }
        return toDomain(saved);
    }

    @Override
    public Optional<CameraSource> findById(Long cameraId) {
        return repository.findById(cameraId).map(this::toDomain);
    }

    @Override
    public List<CameraSource> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long cameraId) {
        repository.deleteById(cameraId);
    }

    private CameraSource toDomain(CameraSourceJpaEntity e) {
        return CameraSource.builder()
                .cameraId(e.getCameraId())
                .organizationId(e.getOrganizationId())
                .userId(e.getUserId())
                .cameraName(e.getCameraName())
                .streamUrl(e.getStreamUrl())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
