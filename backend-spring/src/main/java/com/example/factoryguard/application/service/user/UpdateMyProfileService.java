package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UpdateMyProfileCommand;
import com.example.factoryguard.application.dto.user.UserMeResult;
import com.example.factoryguard.application.port.in.user.UpdateMyProfileUseCase;
import com.example.factoryguard.application.port.out.organization.FindOrganizationByIdPort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.SaveUserPort;
import com.example.factoryguard.application.service.auth.SessionValidationService;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateMyProfileService implements UpdateMyProfileUseCase {

    private static final int NAME_MAX_LENGTH = 50;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-() ]{6,30}$");

    private final FindUserByIdPort findUserByIdPort;
    private final SaveUserPort saveUserPort;
    private final FindOrganizationByIdPort findOrganizationByIdPort;
    private final SessionValidationService sessionValidationService;

    @Override
    public UserMeResult execute(UpdateMyProfileCommand command) {
        Long userId = command.getUserId();
        sessionValidationService.validate(userId, command.getSessionId());

        User user = findUserByIdPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        switch (user.getStatus()) {
            case ACTIVE -> { /* 정상 진행 */ }
            case PENDING -> throw new BusinessException(ErrorCode.PENDING_APPROVAL);
            case REJECTED -> throw new BusinessException(ErrorCode.ACCOUNT_REJECTED);
            default -> throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String nextName = resolveName(command.getName(), user.getName());
        String nextPhone = resolvePhone(command.getPhone(), user.getPhone());

        User updated = user.toBuilder()
                .name(nextName)
                .phone(nextPhone)
                .build();

        User saved;
        try {
            saved = saveUserPort.save(updated);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "내 정보를 저장하지 못했습니다.");
        }
        return UserMeResult.from(saved, resolveOrganizationName(saved.getOrganizationId()));
    }

    private String resolveName(String incoming, String current) {
        if (incoming == null) {
            return current;
        }
        String trimmed = incoming.trim();
        if (trimmed.isEmpty() || trimmed.length() > NAME_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "이름은 1~50자 사이여야 합니다.");
        }
        return trimmed;
    }

    private String resolvePhone(String incoming, String current) {
        if (incoming == null) {
            return current;
        }
        String trimmed = incoming.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "전화번호 형식이 올바르지 않습니다.");
        }
        return trimmed;
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
