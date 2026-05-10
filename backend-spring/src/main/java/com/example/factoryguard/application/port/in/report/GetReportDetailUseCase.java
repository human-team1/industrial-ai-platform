package com.example.factoryguard.application.port.in.report;

import com.example.factoryguard.application.dto.report.ReportDetail;

public interface GetReportDetailUseCase {

    ReportDetail execute(Long reportId);
}
