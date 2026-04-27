package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.out.inspection.SaveInspectionRunPort;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InspectionRunRecorder {

    private final SaveInspectionRunPort saveInspectionRunPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InspectionRun record(InspectionRun run) {
        return saveInspectionRunPort.save(run);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(InspectionRun run) {
        saveInspectionRunPort.save(InspectionRun.builder()
                .inspectionId(run.getInspectionId())
                .organizationId(run.getOrganizationId())
                .userId(run.getUserId())
                .targetId(run.getTargetId())
                .runType(run.getRunType())
                .inputType(run.getInputType())
                .sourceType(run.getSourceType())
                .sourceId(run.getSourceId())
                .runStatus(RunStatus.FAILED)
                .appliedThreshold(run.getAppliedThreshold())
                .idempotencyKey(run.getIdempotencyKey())
                .startedAt(run.getStartedAt())
                .build());
    }
}