package com.example.factoryguard.domain.report.model;

import com.example.factoryguard.domain.report.vo.ReportStatus;
import com.example.factoryguard.domain.report.vo.ReportType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class Report {

    private final Long reportId;
    private final Long organizationId;
    private final ReportType reportType;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final ReportStatus reportStatus;
    private final LocalDateTime snapshotAt;
    private final LocalDateTime generatedAt;
    private final Long generatedBy;
}
