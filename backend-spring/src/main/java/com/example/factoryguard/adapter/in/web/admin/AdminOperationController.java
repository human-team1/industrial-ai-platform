package com.example.factoryguard.adapter.in.web.admin;

import com.example.factoryguard.application.dto.operation.*;
import com.example.factoryguard.application.port.in.operation.*;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminOperationController {

    private final GetSystemStatusUseCase getSystemStatusUseCase;
    private final ListSystemComponentsUseCase listSystemComponentsUseCase;
    private final ListOperationLogsUseCase listOperationLogsUseCase;
    private final GetOperationPolicyUseCase getOperationPolicyUseCase;
    private final UpdateOperationPolicyUseCase updateOperationPolicyUseCase;
    private final ListAuditLogsUseCase listAuditLogsUseCase;
    private final ListAdminActionLogsUseCase listAdminActionLogsUseCase;
    private final ListAsyncJobsUseCase listAsyncJobsUseCase;
    private final GetAsyncJobDetailUseCase getAsyncJobDetailUseCase;
    private final SecurityUtils securityUtils;

    @GetMapping("/system-status")
    public ResponseEntity<ApiResponse<SystemStatusResult>> getSystemStatus() {
        return ResponseEntity.ok(ApiResponse.success(getSystemStatusUseCase.execute(), "시스템 상태를 조회했습니다."));
    }

    @GetMapping("/system-components")
    public ResponseEntity<ApiResponse<List<SystemComponentStatusResult>>> getSystemComponents() {
        return ResponseEntity.ok(ApiResponse.success(listSystemComponentsUseCase.executeComponents(), "시스템 컴포넌트 상태를 조회했습니다."));
    }

    @GetMapping("/operation-logs")
    public ResponseEntity<ApiResponse<OperationPageResponse<OperationLogResult>>> getOperationLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String sourceComponent,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String eventStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ListOperationLogsQuery query = new ListOperationLogsQuery(level, sourceComponent, eventType, eventStatus, startDate, endDate, sort, page, size);
        return ResponseEntity.ok(ApiResponse.success(listOperationLogsUseCase.execute(query), "운영 로그를 조회했습니다."));
    }

    @GetMapping("/operation-policies")
    public ResponseEntity<ApiResponse<List<OperationPolicyResult>>> getOperationPolicies(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                getOperationPolicyUseCase.execute(new ListOperationPoliciesQuery(category, activeOnly)),
                "운영 정책을 조회했습니다."
        ));
    }

    @PatchMapping("/operation-policies/{policyId}")
    public ResponseEntity<ApiResponse<OperationPolicyResult>> updateOperationPolicy(
            @PathVariable Long policyId,
            @RequestBody UpdateOperationPolicyRequest request
    ) {
        OperationPolicyResult result = updateOperationPolicyUseCase.execute(new UpdateOperationPolicyCommand(
                securityUtils.getCurrentUserId(),
                policyId,
                request.getPolicyValue(),
                request.getIsActive()
        ));
        return ResponseEntity.ok(ApiResponse.success(result, "운영 정책을 수정했습니다."));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<OperationPageResponse<AuditLogResult>>> getAuditLogs(
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                listAuditLogsUseCase.execute(new ListAuditLogsQuery(actorUserId, actionType, targetType, startDate, endDate, page, size)),
                "감사 로그를 조회했습니다."
        ));
    }

    @GetMapping("/action-logs")
    public ResponseEntity<ApiResponse<OperationPageResponse<AdminActionLogResult>>> getActionLogs(
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                listAdminActionLogsUseCase.executeAdminActions(new ListAuditLogsQuery(actorUserId, actionType, targetType, startDate, endDate, page, size)),
                "관리자 작업 로그를 조회했습니다."
        ));
    }

    @GetMapping("/async-jobs")
    public ResponseEntity<ApiResponse<OperationPageResponse<AsyncJobSummary>>> getAsyncJobs(
            @RequestParam(required = false) String jobStatus,
            @RequestParam(required = false) String jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                listAsyncJobsUseCase.execute(new ListAsyncJobsQuery(jobStatus, jobType, page, size)),
                "비동기 작업 목록을 조회했습니다."
        ));
    }

    @GetMapping("/async-jobs/{jobId}")
    public ResponseEntity<ApiResponse<AsyncJobDetail>> getAsyncJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(getAsyncJobDetailUseCase.execute(jobId), "비동기 작업 상세를 조회했습니다."));
    }
}
