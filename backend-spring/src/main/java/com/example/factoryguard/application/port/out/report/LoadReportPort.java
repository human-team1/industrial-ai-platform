package com.example.factoryguard.application.port.out.report;

import com.example.factoryguard.domain.report.model.Report;
import com.example.factoryguard.domain.report.model.ReportFile;
import com.example.factoryguard.domain.report.model.ReportItem;

import java.util.List;
import java.util.Optional;

public interface LoadReportPort {

    Optional<Report> findById(Long reportId);

    List<Report> findAllByOrganizationId(Long organizationId);

    List<ReportItem> findItemsByReportId(Long reportId);

    Optional<ReportFile> findFileByReportId(Long reportId);
}
