package com.example.factoryguard.application.dto.report;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportFileResult {

    private final Long reportFileId;
    private final Long reportId;
    private final Long fileId;
}
