package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.dto.auth.SignupRequestCommand;
import com.example.factoryguard.application.dto.auth.SignupRequestResult;
import com.example.factoryguard.application.port.in.auth.SignupRequestUseCase;
import com.example.factoryguard.application.port.out.organization.LoadOrganizationByIdPort;
import com.example.factoryguard.application.port.out.signuprequest.SaveSignupRequestPort;
import com.example.factoryguard.application.port.out.user.FindUserByGoogleSubPort;
import com.example.factoryguard.application.port.out.user.SaveUserPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.organization.model.Organization;
import com.example.factoryguard.domain.organization.vo.OrganizationStatus;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserRole;
import com.example.factoryguard.domain.user.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SignupService implements SignupRequestUseCase {

    private final FindUserByGoogleSubPort findUserByGoogleSubPort;
    private final SaveUserPort saveUserPort;
    private final SaveSignupRequestPort saveSignupRequestPort;
    private final LoadOrganizationByIdPort loadOrganizationByIdPort;

    @Override
    public SignupRequestResult execute(SignupRequestCommand command) {
        Organization organization = loadOrganizationByIdPort.findById(command.getOrganizationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND));
        if (organization.getStatus() != OrganizationStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ORGANIZATION_INACTIVE);
        }

        return findUserByGoogleSubPort.findByGoogleSub(command.getGoogleSub())
                .map(existing -> SignupRequestResult.builder()
                        .userId(existing.getUserId())
                        .status(existing.getStatus().name())
                        .build())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .googleSub(command.getGoogleSub())
                            .email(command.getEmail())
                            .name(command.getName())
                            .picture(command.getPicture())
                            .phone(command.getPhone())
                            .organizationId(command.getOrganizationId())
                            .status(UserStatus.PENDING)
                            .role(UserRole.ROLE_COMPANY_WORKER)
                            .build();
                    User saved = saveUserPort.save(newUser);
                    saveSignupRequestPort.saveSignupRequest(saved.getUserId(), command.getOrganizationId());
                    return SignupRequestResult.builder()
                            .userId(saved.getUserId())
                            .status(saved.getStatus().name())
                            .build();
                });
    }
}
