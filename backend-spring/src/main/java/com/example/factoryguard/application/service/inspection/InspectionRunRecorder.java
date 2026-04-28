package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionRunPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
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
    private final LoadInspectionRunPort loadInspectionRunPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InspectionRun create(InspectionRun run) {
        return saveInspectionRunPort.save(run);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void transitTo(Long inspectionId, RunStatus next) {
        InspectionRun existing = loadInspectionRunPort.findRunById(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_NOT_FOUND));
        saveInspectionRunPort.save(existing.toBuilder().runStatus(next).build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long inspectionId, String errorCode) {
        InspectionRun existing = loadInspectionRunPort.findRunById(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_NOT_FOUND));
        saveInspectionRunPort.save(existing.toBuilder()
                .runStatus(RunStatus.FAILED)
                .errorCode(errorCode)
                .build());
    }
}
