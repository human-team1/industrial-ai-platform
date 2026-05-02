package com.example.factoryguard.application.service.signup;

import com.example.factoryguard.application.dto.signup.SignupRequestSummary;
import com.example.factoryguard.application.dto.operation.RecordAdminActionLogCommand;
import com.example.factoryguard.application.dto.operation.RecordAuditLogCommand;
import com.example.factoryguard.application.port.in.operation.RecordAdminActionLogUseCase;
import com.example.factoryguard.application.port.in.operation.RecordAuditLogUseCase;
import com.example.factoryguard.application.port.in.signup.ApproveSignupUseCase;
import com.example.factoryguard.application.port.in.signup.GetSignupRequestsUseCase;
import com.example.factoryguard.application.port.in.signup.RejectSignupUseCase;
import com.example.factoryguard.application.port.out.signuprequest.FindPendingSignupRequestsPort;
import com.example.factoryguard.application.port.out.signuprequest.ProcessSignupRequestPort;
import com.example.factoryguard.application.port.out.user.UpdateUserStatusPort;
import com.example.factoryguard.domain.user.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignupAdminService implements GetSignupRequestsUseCase, ApproveSignupUseCase, RejectSignupUseCase {

    private final FindPendingSignupRequestsPort findPendingSignupRequestsPort;
    private final ProcessSignupRequestPort processSignupRequestPort;
    private final UpdateUserStatusPort updateUserStatusPort;
    private final RecordAdminActionLogUseCase recordAdminActionLogUseCase;
    private final RecordAuditLogUseCase recordAuditLogUseCase;

    @Override
    @Transactional(readOnly = true)
    public List<SignupRequestSummary> execute() {
        return findPendingSignupRequestsPort.findPending();
    }

    @Override
    @Transactional
    public void execute(Long requestId, Long adminUserId, Long organizationId) {
        // organizationId는 신청 시점에 USERS.organization_id로 이미 저장됨.
        // 승인 시 별도 할당 호출은 중복이므로 수행하지 않음.
        Long userId = processSignupRequestPort.approve(requestId, adminUserId);
        updateUserStatusPort.updateStatus(userId, UserStatus.ACTIVE);
        recordAdminActionLogUseCase.recordAdminActionLog(RecordAdminActionLogCommand.builder()
                .actorUserId(adminUserId)
                .actionType("APPROVE_SIGNUP")
                .targetType("SIGNUP_REQUEST")
                .targetId(requestId)
                .reason("회원가입 신청 승인")
                .build());
        recordAuditLogUseCase.recordAuditLog(RecordAuditLogCommand.builder()
                .actorUserId(adminUserId)
                .actionType("UPDATE")
                .targetType("USER")
                .targetId(userId)
                .beforeJson("{\"status\":\"PENDING\"}")
                .afterJson("{\"status\":\"ACTIVE\"}")
                .build());
    }

    @Override
    @Transactional
    public void execute(Long requestId, Long adminUserId, String rejectReason) {
        Long userId = processSignupRequestPort.reject(requestId, adminUserId, rejectReason);
        updateUserStatusPort.updateStatus(userId, UserStatus.REJECTED);
        recordAdminActionLogUseCase.recordAdminActionLog(RecordAdminActionLogCommand.builder()
                .actorUserId(adminUserId)
                .actionType("REJECT_SIGNUP")
                .targetType("SIGNUP_REQUEST")
                .targetId(requestId)
                .reason("회원가입 신청 거절")
                .build());
        recordAuditLogUseCase.recordAuditLog(RecordAuditLogCommand.builder()
                .actorUserId(adminUserId)
                .actionType("UPDATE")
                .targetType("USER")
                .targetId(userId)
                .beforeJson("{\"status\":\"PENDING\"}")
                .afterJson("{\"status\":\"REJECTED\"}")
                .build());
    }
}
