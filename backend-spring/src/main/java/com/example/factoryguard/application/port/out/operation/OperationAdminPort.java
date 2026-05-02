package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.application.dto.operation.*;

import java.util.List;
import java.util.Optional;

public interface OperationAdminPort {

    Optional<SystemStatusResult> findLatestSystemStatus();

    List<SystemComponentStatusResult> findSystemComponents();

    OperationPageResponse<OperationLogResult> findOperationLogs(ListOperationLogsQuery query);

    List<OperationPolicyResult> findOperationPolicies(ListOperationPoliciesQuery query);

    Optional<OperationPolicyResult> findOperationPolicy(Long policyId);

    OperationPolicyResult updateOperationPolicy(Long policyId, String policyValue, Boolean isActive, Long updatedBy);

    OperationPageResponse<AuditLogResult> findAuditLogs(ListAuditLogsQuery query);

    OperationPageResponse<AdminActionLogResult> findAdminActionLogs(ListAuditLogsQuery query);

    OperationPageResponse<AsyncJobSummary> findAsyncJobs(ListAsyncJobsQuery query);

    Optional<AsyncJobDetail> findAsyncJob(Long jobId);

    void saveAdminActionLog(Long actorUserId, String actionType, String targetType, Long targetId, String reason);

    void saveAuditLog(Long actorUserId, String actionType, String targetType, Long targetId, String beforeJson, String afterJson);

    void saveOperationLog(String eventType, String eventStatus, String logLevel, String sourceComponent,
                          String requestId, Long actorUserId, String detailMessage, String relatedPath);

    void saveSystemStatusSnapshot(SystemComponentStatusResult status);

    void saveSystemComponentStatus(SystemComponentStatusResult status);
}
