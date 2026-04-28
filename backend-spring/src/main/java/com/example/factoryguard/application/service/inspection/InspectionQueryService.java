package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.in.inspection.GetInspectionEventsUseCase;
import com.example.factoryguard.application.port.in.inspection.GetInspectionsUseCase;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionEventLogPort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.inspection.model.InspectionEventLog;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InspectionQueryService implements GetInspectionsUseCase, GetInspectionEventsUseCase {

    private final LoadInspectionRunPort loadInspectionRunPort;
    private final LoadInspectionEventLogPort loadInspectionEventLogPort;

    @Override
    public List<InspectionRun> list(Long organizationId, int page, int size) {
        return loadInspectionRunPort.findRunsByOrganizationId(organizationId, page, size);
    }

    @Override
    public InspectionRun detail(Long organizationId, Long inspectionId) {
        InspectionRun run = loadInspectionRunPort.findRunById(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_NOT_FOUND));
        if (!run.getOrganizationId().equals(organizationId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return run;
    }

    @Override
    public List<InspectionEventLog> execute(Long organizationId, Long inspectionId) {
        InspectionRun run = loadInspectionRunPort.findRunById(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_NOT_FOUND));
        if (!run.getOrganizationId().equals(organizationId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return loadInspectionEventLogPort.findByInspectionIdOrderByCreatedAt(inspectionId);
    }
}
