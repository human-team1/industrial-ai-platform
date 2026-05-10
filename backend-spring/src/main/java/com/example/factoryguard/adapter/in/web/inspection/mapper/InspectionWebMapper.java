package com.example.factoryguard.adapter.in.web.inspection.mapper;

import com.example.factoryguard.adapter.in.web.inspection.dto.CameraSourceResponse;
import com.example.factoryguard.adapter.in.web.inspection.dto.InspectionEventLogResponse;
import com.example.factoryguard.adapter.in.web.inspection.dto.InspectionRunResponse;
import com.example.factoryguard.adapter.in.web.inspection.dto.InspectionStatusResponse;
import com.example.factoryguard.domain.inspection.model.CameraSource;
import com.example.factoryguard.domain.inspection.model.InspectionEventLog;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.InspectionStatus;
import org.springframework.stereotype.Component;

@Component
public class InspectionWebMapper {

    public InspectionStatusResponse toResponse(InspectionStatus s) {
        return new InspectionStatusResponse(s.getService(), s.getStatus());
    }

    public InspectionRunResponse toResponse(InspectionRun r) {
        return new InspectionRunResponse(
                r.getInspectionId(),
                r.getOrganizationId(),
                r.getUserId(),
                r.getTargetId(),
                r.getRunType(),
                r.getRunStatus(),
                r.getAppliedThreshold(),
                r.getErrorCode(),
                r.getStartedAt(),
                r.getCompletedAt()
        );
    }

    public InspectionEventLogResponse toResponse(InspectionEventLog e) {
        return new InspectionEventLogResponse(
                e.getEventId(),
                e.getInspectionId(),
                e.getEventType(),
                e.getMessage(),
                e.getCreatedAt()
        );
    }

    public CameraSourceResponse toResponse(CameraSource c) {
        return new CameraSourceResponse(
                c.getCameraId(),
                c.getOrganizationId(),
                c.getUserId(),
                c.getCameraName(),
                c.getStreamUrl(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
