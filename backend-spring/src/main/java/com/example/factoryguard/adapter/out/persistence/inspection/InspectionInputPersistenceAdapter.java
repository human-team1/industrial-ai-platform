package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.application.port.out.inspection.LoadInspectionInputPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionInputPort;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InspectionInputPersistenceAdapter implements SaveInspectionInputPort, LoadInspectionInputPort {

    private final InspectionInputJpaRepository repository;

    @Override
    public InspectionInput save(InspectionInput input) {
        InspectionInputJpaEntity saved = repository.save(InspectionInputJpaEntity.builder()
                .inspectionId(input.getInspectionId())
                .fileId(input.getFileId())
                .cameraId(input.getCameraId())
                .streamUrl(input.getStreamUrl())
                .sourceType(input.getSourceType())
                .sourceName(input.getSourceName())
                .mimeType(input.getMimeType())
                .durationSec(input.getDurationSec())
                .frameCount(input.getFrameCount())
                .build());
        return toDomain(saved);
    }

    @Override
    public List<InspectionInput> findByInspectionId(Long inspectionId) {
        return repository.findByInspectionIdOrderByCreatedAtAsc(inspectionId).stream()
                .map(this::toDomain)
                .toList();
    }

    private InspectionInput toDomain(InspectionInputJpaEntity e) {
        return InspectionInput.builder()
                .inspectionInputId(e.getInspectionInputId())
                .inspectionId(e.getInspectionId())
                .fileId(e.getFileId())
                .cameraId(e.getCameraId())
                .streamUrl(e.getStreamUrl())
                .sourceType(e.getSourceType())
                .sourceName(e.getSourceName())
                .mimeType(e.getMimeType())
                .durationSec(e.getDurationSec())
                .frameCount(e.getFrameCount())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
