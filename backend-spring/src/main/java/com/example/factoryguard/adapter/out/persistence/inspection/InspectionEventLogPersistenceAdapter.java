package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.adapter.out.persistence.inspection.mapper.InspectionPersistenceMapper;
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
    private final InspectionPersistenceMapper mapper;

    @Override
    public InspectionEventLog save(InspectionEventLog log) {
        InspectionEventLogJpaEntity saved = repository.save(mapper.toEntity(log));
        return mapper.toDomain(saved);
    }

    @Override
    public List<InspectionEventLog> findByInspectionIdOrderByCreatedAt(Long inspectionId) {
        return repository.findByInspectionIdOrderByCreatedAtAsc(inspectionId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
