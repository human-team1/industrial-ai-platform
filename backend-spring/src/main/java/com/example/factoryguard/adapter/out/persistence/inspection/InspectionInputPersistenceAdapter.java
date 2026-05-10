package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.adapter.out.persistence.inspection.mapper.InspectionPersistenceMapper;
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
    private final InspectionPersistenceMapper mapper;

    @Override
    public InspectionInput save(InspectionInput input) {
        InspectionInputJpaEntity saved = repository.save(mapper.toEntity(input));
        return mapper.toDomain(saved);
    }

    @Override
    public List<InspectionInput> findByInspectionId(Long inspectionId) {
        return repository.findByInspectionIdOrderByCreatedAtAsc(inspectionId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
