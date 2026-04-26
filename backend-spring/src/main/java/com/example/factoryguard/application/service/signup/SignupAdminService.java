package com.example.factoryguard.application.service.signup;

import com.example.factoryguard.application.dto.signup.SignupRequestSummary;
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

    @Override
    @Transactional(readOnly = true)
    public List<SignupRequestSummary> execute() {
        return findPendingSignupRequestsPort.findPending();
    }

    @Override
    @Transactional
    public void execute(Long requestId, Long adminUserId) {
        Long userId = processSignupRequestPort.approve(requestId, adminUserId);
        updateUserStatusPort.updateStatus(userId, UserStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void execute(Long requestId, Long adminUserId, String rejectReason) {
        Long userId = processSignupRequestPort.reject(requestId, adminUserId, rejectReason);
        updateUserStatusPort.updateStatus(userId, UserStatus.REJECTED);
    }
}
