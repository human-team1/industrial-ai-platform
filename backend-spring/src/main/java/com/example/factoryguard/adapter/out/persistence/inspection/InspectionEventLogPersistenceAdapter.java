package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.application.port.out.inspection.LoadInspectionEventLogPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionEventLogPort;
import com.example.factoryguard.domain.inspection.model.InspectionEventLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InspectionEventLogPersistenceAdapter implements SaveInspectionEventLogPort, LoadInspectionEventLogPort {

    private final InspectionEventLogJpaRepository repository;

    @Override
    public InspectionEventLog save(InspectionEventLog log) {
        InspectionEventLogJpaEntity saved = repository.save(InspectionEventLogJpaEntity.builder()
                .inspectionId(log.getInspectionId())
                .eventType(log.getEventType())
                .message(log.getMessage())
                .build());
        return toDomain(saved);
    }

    @Override
    public List<InspectionEventLog> findByInspectionIdOrderByCreatedAt(Long inspectionId) {
        return repository.findByInspectionIdOrderByCreatedAtAsc(inspectionId).stream()
                .map(this::toDomain)
                .toList();
    }

    private InspectionEventLog toDomain(InspectionEventLogJpaEntity e) {
        return InspectionEventLog.builder()
                .eventId(e.getEventId())
                .inspectionId(e.getInspectionId())
                .eventType(e.getEventType())
                .message(e.getMessage())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
