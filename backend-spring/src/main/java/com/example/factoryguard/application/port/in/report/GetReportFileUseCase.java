package com.example.factoryguard.application.port.in.report;

import com.example.factoryguard.application.dto.report.ReportFileResult;

public interface GetReportFileUseCase {

    ReportFileResult execute(Long reportId);
}
