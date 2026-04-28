package com.example.factoryguard.application.port.in.report;

import com.example.factoryguard.application.dto.report.GenerateReportCommand;
import com.example.factoryguard.application.dto.report.ReportDetail;

public interface GenerateReportUseCase {

    ReportDetail execute(GenerateReportCommand command);
}
