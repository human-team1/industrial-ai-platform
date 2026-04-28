package com.example.factoryguard.application.dto.report;

import com.example.factoryguard.domain.report.vo.ReportType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class GenerateReportCommand {

    private final Long organizationId;
    private final Long generatedBy;
    private final ReportType reportType;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
}
