package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.adapter.out.persistence.inspection.mapper.InspectionPersistenceMapper;
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
    private final InspectionPersistenceMapper mapper;

    @Override
    public CameraSource save(CameraSource camera) {
        CameraSourceJpaEntity saved;
        if (camera.getCameraId() != null) {
            CameraSourceJpaEntity existing = repository.findById(camera.getCameraId()).orElseThrow();
            existing.update(camera.getCameraName(), camera.getStreamUrl(), camera.getStatus());
            saved = existing;
        } else {
            saved = repository.save(mapper.toEntity(camera));
        }
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CameraSource> findById(Long cameraId) {
        return repository.findById(cameraId).map(mapper::toDomain);
    }

    @Override
    public List<CameraSource> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long cameraId) {
        repository.deleteById(cameraId);
    }
}
