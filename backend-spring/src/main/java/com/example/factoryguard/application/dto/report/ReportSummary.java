package com.example.factoryguard.application.dto.report;

import com.example.factoryguard.domain.report.vo.ReportStatus;
import com.example.factoryguard.domain.report.vo.ReportType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportSummary {

    private final Long reportId;
    private final ReportType reportType;
    private final ReportStatus reportStatus;
}
