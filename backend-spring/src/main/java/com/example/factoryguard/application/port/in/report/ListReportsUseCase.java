package com.example.factoryguard.application.port.in.report;

import com.example.factoryguard.application.dto.report.ReportSummary;

import java.util.List;

public interface ListReportsUseCase {

    List<ReportSummary> execute(Long organizationId);
}
