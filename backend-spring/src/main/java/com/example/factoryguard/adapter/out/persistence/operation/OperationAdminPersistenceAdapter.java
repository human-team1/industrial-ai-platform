package com.example.factoryguard.adapter.out.persistence.operation;

import com.example.factoryguard.application.dto.operation.*;
import com.example.factoryguard.application.port.out.operation.OperationAdminPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OperationAdminPersistenceAdapter implements OperationAdminPort {

    private final OperationAdminQueryRepository repository;

    @Override
    public Optional<SystemStatusResult> findLatestSystemStatus() {
        return repository.findLatestSystemStatus();
    }

    @Override
    public List<SystemComponentStatusResult> findSystemComponents() {
        return repository.findSystemComponents();
    }

    @Override
    public OperationPageResponse<OperationLogResult> findOperationLogs(ListOperationLogsQuery query) {
        return repository.findOperationLogs(query);
    }

    @Override
    public List<OperationPolicyResult> findOperationPolicies(ListOperationPoliciesQuery query) {
        return repository.findOperationPolicies(query);
    }

    @Override
    public Optional<OperationPolicyResult> findOperationPolicy(Long policyId) {
        return repository.findOperationPolicy(policyId);
    }

    @Override
    public OperationPolicyResult updateOperationPolicy(Long policyId, String policyValue, Boolean isActive, Long updatedBy) {
        return repository.updateOperationPolicy(policyId, policyValue, isActive, updatedBy);
    }

    @Override
    public OperationPageResponse<AuditLogResult> findAuditLogs(ListAuditLogsQuery query) {
        return repository.findAuditLogs(query);
    }

    @Override
    public OperationPageResponse<AdminActionLogResult> findAdminActionLogs(ListAuditLogsQuery query) {
        return repository.findAdminActionLogs(query);
    }

    @Override
    public OperationPageResponse<AsyncJobSummary> findAsyncJobs(ListAsyncJobsQuery query) {
        return repository.findAsyncJobs(query);
    }

    @Override
    public Optional<AsyncJobDetail> findAsyncJob(Long jobId) {
        return repository.findAsyncJob(jobId);
    }

    @Override
    public void saveAdminActionLog(Long actorUserId, String actionType, String targetType, Long targetId, String reason) {
        repository.saveAdminActionLog(actorUserId, actionType, targetType, targetId, reason);
    }

    @Override
    public void saveAuditLog(Long actorUserId, String actionType, String targetType, Long targetId, String beforeJson, String afterJson) {
        repository.saveAuditLog(actorUserId, actionType, targetType, targetId, beforeJson, afterJson);
    }

    @Override
    public void saveOperationLog(String eventType, String eventStatus, String logLevel, String sourceComponent,
                                 String requestId, Long actorUserId, String detailMessage, String relatedPath) {
        repository.saveOperationLog(eventType, eventStatus, logLevel, sourceComponent, requestId, actorUserId, detailMessage, relatedPath);
    }

    @Override
    public void saveSystemStatusSnapshot(SystemComponentStatusResult status) {
        repository.saveSystemStatusSnapshot(status);
    }

    @Override
    public void saveSystemComponentStatus(SystemComponentStatusResult status) {
        repository.saveSystemComponentStatus(status);
    }
}
