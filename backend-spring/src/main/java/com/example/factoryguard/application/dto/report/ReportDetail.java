package com.example.factoryguard.application.dto.report;

import com.example.factoryguard.domain.report.vo.ReportStatus;
import com.example.factoryguard.domain.report.vo.ReportType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ReportDetail {

    private final Long reportId;
    private final Long organizationId;
    private final ReportType reportType;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final ReportStatus reportStatus;
}
