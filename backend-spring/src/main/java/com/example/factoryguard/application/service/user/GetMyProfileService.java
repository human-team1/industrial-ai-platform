package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UserMeResult;
import com.example.factoryguard.application.port.in.user.GetMyProfileUseCase;
import com.example.factoryguard.application.port.out.organization.FindOrganizationByIdPort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.service.auth.SessionValidationService;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyProfileService implements GetMyProfileUseCase {

    private final FindUserByIdPort findUserByIdPort;
    private final FindOrganizationByIdPort findOrganizationByIdPort;
    private final SessionValidationService sessionValidationService;

    @Override
    public UserMeResult execute(Long userId, String sessionId) {
        sessionValidationService.validate(userId, sessionId);

        User user = findUserByIdPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        return switch (user.getStatus()) {
            case ACTIVE -> UserMeResult.from(user, resolveOrganizationName(user.getOrganizationId()));
            case PENDING -> throw new BusinessException(ErrorCode.PENDING_APPROVAL);
            case REJECTED -> throw new BusinessException(ErrorCode.ACCOUNT_REJECTED);
            default -> throw new BusinessException(ErrorCode.UNAUTHORIZED);
        };
    }

    private String resolveOrganizationName(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        return findOrganizationByIdPort.findById(organizationId)
                .map(o -> o.getOrganizationName())
                .orElse(null);
    }
}
