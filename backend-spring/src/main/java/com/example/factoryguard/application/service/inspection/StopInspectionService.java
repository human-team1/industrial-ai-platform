package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.in.inspection.StopInspectionUseCase;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.inspection.model.InspectionEventType;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StopInspectionService implements StopInspectionUseCase {

    private final LoadInspectionRunPort loadInspectionRunPort;
    private final InspectionRunRecorder runRecorder;
    private final InspectionEventLogger eventLogger;

    @Override
    public InspectionRun execute(Long userId, Long organizationId, Long inspectionId) {
        InspectionRun run = loadInspectionRunPort.findRunById(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_NOT_FOUND));
        if (!run.getOrganizationId().equals(organizationId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (run.getRunStatus() != RunStatus.PENDING
                && run.getRunStatus() != RunStatus.PROCESSING) {
            throw new BusinessException(ErrorCode.INSPECTION_INVALID_STATE);
        }
        try {
            runRecorder.transitTo(inspectionId, RunStatus.STOPPED);
            eventLogger.log(inspectionId, InspectionEventType.REALTIME_STOPPED, "실시간 탐지가 중지되었습니다.");
            return loadInspectionRunPort.findRunById(inspectionId)
                    .orElse(run.toBuilder().runStatus(RunStatus.STOPPED).build());
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("Unexpected error during stop, runId={}", inspectionId, e);
            runRecorder.markFailed(inspectionId, "UNEXPECTED_ERROR");
            eventLogger.logFailure(inspectionId, InspectionEventType.FAILED, "stop failed: " + e.getMessage());
            throw new BusinessException(ErrorCode.INSPECTION_FAILED);
        }
    }
}
