package com.example.factoryguard.domain.inspection.model;

public class InspectionStatus {

    private final String service;
    private final String status;

    public InspectionStatus(String service, String status) {
        this.service = service;
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public String getStatus() {
        return status;
    }
}
