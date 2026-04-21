package com.example.factoryguard.application.dto.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionStatus;

public class InspectionStatusResponse {

    private final String service;
    private final String status;

    private InspectionStatusResponse(String service, String status) {
        this.service = service;
        this.status = status;
    }

    public static InspectionStatusResponse from(InspectionStatus inspectionStatus) {
        return new InspectionStatusResponse(inspectionStatus.getService(), inspectionStatus.getStatus());
    }

    public String getService() {
        return service;
    }

    public String getStatus() {
        return status;
    }
}
