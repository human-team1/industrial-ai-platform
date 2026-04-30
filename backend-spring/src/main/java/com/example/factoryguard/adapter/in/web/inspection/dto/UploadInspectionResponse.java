package com.example.factoryguard.adapter.in.web.inspection.dto;

import com.example.factoryguard.domain.inspection.model.RunStatus;

public record UploadInspectionResponse(
        Long inspectionId,
        RunStatus runStatus
) {
}
