package com.example.factoryguard.application.port.out.report;

import com.example.factoryguard.domain.report.model.Report;
import com.example.factoryguard.domain.report.model.ReportFile;
import com.example.factoryguard.domain.report.model.ReportItem;

public interface SaveReportPort {

    Report saveReport(Report report);

    ReportItem saveItem(ReportItem reportItem);

    ReportFile saveFile(ReportFile reportFile);
}
