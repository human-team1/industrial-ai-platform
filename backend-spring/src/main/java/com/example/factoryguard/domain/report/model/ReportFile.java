package com.example.factoryguard.domain.report.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportFile {

    private final Long reportFileId;
    private final Long reportId;
    private final Long fileId;
}
