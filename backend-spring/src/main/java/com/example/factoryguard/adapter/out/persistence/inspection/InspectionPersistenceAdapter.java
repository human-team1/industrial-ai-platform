package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.adapter.out.persistence.inspection.mapper.InspectionPersistenceMapper;
import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionResultPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionRunPort;
import com.example.factoryguard.application.port.out.result.LoadInspectionResultPort;
import com.example.factoryguard.domain.inspection.model.AnalysisTarget;
import com.example.factoryguard.domain.inspection.model.InspectionResult;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InspectionPersistenceAdapter implements
        LoadAnalysisTargetPort,
        SaveInspectionRunPort,
        LoadInspectionRunPort,
        SaveInspectionResultPort,
        LoadInspectionResultPort {

    private final AnalysisTargetJpaRepository analysisTargetJpaRepository;
    private final InspectionRunJpaRepository inspectionRunJpaRepository;
    private final InspectionResultJpaRepository inspectionResultJpaRepository;
    private final InspectionPersistenceMapper mapper;

    @Override
    public Optional<AnalysisTarget> findById(Long targetId) {
        return analysisTargetJpaRepository.findById(targetId).map(mapper::toDomain);
    }

    @Override
    public List<AnalysisTarget> findByOrganizationId(Long organizationId) {
        return analysisTargetJpaRepository.findByOrganizationIdOrderByTargetIdAsc(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public InspectionRun save(InspectionRun run) {
        InspectionRunJpaEntity saved;
        if (run.getInspectionId() != null) {
            InspectionRunJpaEntity existing = inspectionRunJpaRepository.findById(run.getInspectionId())
                    .orElseThrow();
            existing.updateStatus(run.getRunStatus());
            if (run.getErrorCode() != null) {
                existing.updateError(run.getErrorCode());
            }
            saved = existing;
        } else {
            saved = inspectionRunJpaRepository.save(mapper.toEntity(run));
        }
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<InspectionRun> findByOrganizationIdAndUserIdAndIdempotencyKey(
            Long organizationId, Long userId, String idempotencyKey) {
        return inspectionRunJpaRepository
                .findByOrganizationIdAndUserIdAndIdempotencyKey(organizationId, userId, idempotencyKey)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<InspectionRun> findRunById(Long inspectionId) {
        return inspectionRunJpaRepository.findById(inspectionId).map(mapper::toDomain);
    }

    @Override
    public List<InspectionRun> findRunsByOrganizationId(Long organizationId, int page, int size) {
        return inspectionRunJpaRepository.findByOrganizationId(
                organizationId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"))
        ).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<InspectionRun> findRunsByStatusAndStartedAtBefore(RunStatus status, LocalDateTime threshold) {
        return inspectionRunJpaRepository.findByRunStatusAndStartedAtBefore(status, threshold).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public InspectionResult save(InspectionResult result) {
        InspectionResultJpaEntity saved = inspectionResultJpaRepository.save(mapper.toEntity(result));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<InspectionResult> findResultById(Long resultId) {
        return inspectionResultJpaRepository.findById(resultId).map(mapper::toDomain);
    }

    @Override
    public List<InspectionResult> findAllByInspectionId(Long inspectionId) {
        return inspectionResultJpaRepository.findAllByInspectionIdOrderByCreatedAtAsc(inspectionId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
