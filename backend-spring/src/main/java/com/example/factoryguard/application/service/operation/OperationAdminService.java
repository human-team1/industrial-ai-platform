package com.example.factoryguard.application.service.operation;

import com.example.factoryguard.application.dto.operation.*;
import com.example.factoryguard.application.port.in.operation.*;
import com.example.factoryguard.application.port.out.operation.OperationAdminPort;
import com.example.factoryguard.application.port.out.operation.OperationStatusCachePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationAdminService implements
        GetSystemStatusUseCase,
        ListSystemComponentsUseCase,
        ListOperationLogsUseCase,
        GetOperationPolicyUseCase,
        UpdateOperationPolicyUseCase,
        ListAuditLogsUseCase,
        ListAdminActionLogsUseCase,
        ListAsyncJobsUseCase,
        GetAsyncJobDetailUseCase {

    private final OperationAdminPort operationAdminPort;
    private final OperationStatusCachePort operationStatusCachePort;
    private final SecurityUtils securityUtils;
    private static final List<String> COMPONENT_TYPES = List.of(
            "SPRING_API", "AI_SERVER", "MARIADB", "REDIS", "MINIO", "CHROMA", "STREAM_SERVER", "STORAGE"
    );

    @Override
    @Transactional(readOnly = true)
    public SystemStatusResult execute() {
        requireSiteAdmin();
        SystemComponentStatusResult cached = operationStatusCachePort.findSystemStatus("spring").orElse(null);
        if (cached != null) {
            return SystemStatusResult.builder()
                    .cpuUsage(cached.getCpuUsage())
                    .memoryUsage(cached.getMemoryUsage())
                    .diskUsage(cached.getDiskUsage())
                    .responseTimeMs(cached.getResponseTimeMs())
                    .overallStatus(cached.getStatus())
                    .createdAt(cached.getCheckedAt())
                    .build();
        }
        return operationAdminPort.findLatestSystemStatus()
                .orElseGet(() -> SystemStatusResult.builder()
                        .overallStatus("UNKNOWN")
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemComponentStatusResult> executeComponents() {
        requireSiteAdmin();
        List<SystemComponentStatusResult> cached = operationStatusCachePort.findComponentStatuses(COMPONENT_TYPES);
        List<SystemComponentStatusResult> persisted = operationAdminPort.findSystemComponents();
        if (!cached.isEmpty()) {
            return mergeComponents(cached, persisted);
        }
        return mergeComponents(List.of(), persisted);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationPageResponse<OperationLogResult> execute(ListOperationLogsQuery query) {
        requireSiteAdmin();
        validatePage(query.getPage(), query.getSize());
        return operationAdminPort.findOperationLogs(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationPolicyResult> execute(ListOperationPoliciesQuery query) {
        requireSiteAdmin();
        return operationAdminPort.findOperationPolicies(query);
    }

    @Override
    @Transactional
    public OperationPolicyResult execute(UpdateOperationPolicyCommand command) {
        requireSiteAdmin();
        OperationPolicyResult before = operationAdminPort.findOperationPolicy(command.getPolicyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "운영 정책을 찾을 수 없습니다."));
        String nextValue = command.getPolicyValue() != null ? command.getPolicyValue() : before.getPolicyValue();
        validatePolicyValue(before.getValueType(), nextValue);

        OperationPolicyResult updated = operationAdminPort.updateOperationPolicy(
                command.getPolicyId(),
                nextValue,
                command.getIsActive(),
                command.getAdminUserId()
        );
        operationAdminPort.saveAdminActionLog(
                command.getAdminUserId(),
                "UPDATE_OPERATION_POLICY",
                "OPERATION_POLICY",
                command.getPolicyId(),
                "운영 정책 변경: " + before.getPolicyKey()
        );
        operationAdminPort.saveAuditLog(
                command.getAdminUserId(),
                "UPDATE",
                "OPERATION_POLICY",
                command.getPolicyId(),
                "{\"policyValue\":\"" + mask(before.getPolicyValue()) + "\",\"isActive\":" + before.getIsActive() + "}",
                "{\"policyValue\":\"" + mask(updated.getPolicyValue()) + "\",\"isActive\":" + updated.getIsActive() + "}"
        );
        operationAdminPort.saveOperationLog(
                "OPERATION_POLICY_UPDATED",
                "SUCCESS",
                "INFO",
                "SPRING_API",
                MDC.get("requestId"),
                command.getAdminUserId(),
                "운영 정책이 변경되었습니다: " + before.getPolicyKey(),
                "/api/v1/admin/operation-policies/" + command.getPolicyId()
        );
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public OperationPageResponse<AuditLogResult> execute(ListAuditLogsQuery query) {
        requireSiteAdmin();
        validatePage(query.getPage(), query.getSize());
        return operationAdminPort.findAuditLogs(query);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationPageResponse<AdminActionLogResult> executeAdminActions(ListAuditLogsQuery query) {
        requireSiteAdmin();
        validatePage(query.getPage(), query.getSize());
        return operationAdminPort.findAdminActionLogs(query);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationPageResponse<AsyncJobSummary> execute(ListAsyncJobsQuery query) {
        requireSiteAdmin();
        validatePage(query.getPage(), query.getSize());
        return operationAdminPort.findAsyncJobs(query);
    }

    @Override
    @Transactional(readOnly = true)
    public AsyncJobDetail execute(Long jobId) {
        requireSiteAdmin();
        return operationAdminPort.findAsyncJob(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "비동기 작업을 찾을 수 없습니다."));
    }

    private void requireSiteAdmin() {
        if (!securityUtils.isSiteAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "page는 0 이상, size는 1~100이어야 합니다.");
        }
    }

    private void validatePolicyValue(String valueType, String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "정책 값은 비어 있을 수 없습니다.");
        }
        String type = valueType == null ? "STRING" : valueType;
        try {
            if ("NUMBER".equals(type)) {
                Double.parseDouble(value);
            } else if ("BOOLEAN".equals(type)) {
                String normalized = value.trim().toLowerCase();
                if (!List.of("true", "false", "1", "0", "yes", "no", "on", "off").contains(normalized)) {
                    throw new IllegalArgumentException();
                }
            } else if ("JSON".equals(type)) {
                String trimmed = value.trim();
                if (!(trimmed.startsWith("{") && trimmed.endsWith("}")) && !(trimmed.startsWith("[") && trimmed.endsWith("]"))) {
                    throw new IllegalArgumentException();
                }
            }
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "정책 값 타입이 올바르지 않습니다.");
        }
    }

    private String mask(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private List<SystemComponentStatusResult> mergeComponents(
            List<SystemComponentStatusResult> cached,
            List<SystemComponentStatusResult> fallback
    ) {
        java.util.Map<String, SystemComponentStatusResult> byType = new java.util.LinkedHashMap<>();
        for (SystemComponentStatusResult item : fallback) {
            byType.put(item.getComponentType(), item);
        }
        for (SystemComponentStatusResult item : cached) {
            byType.put(item.getComponentType(), item);
        }
        return COMPONENT_TYPES.stream()
                .map(type -> byType.getOrDefault(type, unknownComponent(type)))
                .toList();
    }

    private SystemComponentStatusResult unknownComponent(String type) {
        LocalDateTime now = LocalDateTime.now();
        return SystemComponentStatusResult.builder()
                .componentType(type)
                .componentName(componentName(type))
                .status("UNKNOWN")
                .message("상태 정보 없음")
                .checkedAt(now)
                .createdAt(now)
                .build();
    }

    private String componentName(String type) {
        return switch (type) {
            case "SPRING_API" -> "Spring API";
            case "AI_SERVER" -> "AI Server";
            case "MARIADB" -> "MariaDB";
            case "REDIS" -> "Redis";
            case "MINIO" -> "MinIO";
            case "CHROMA" -> "ChromaDB";
            case "STREAM_SERVER" -> "Stream Server";
            case "STORAGE" -> "Storage";
            default -> type;
        };
    }
}
