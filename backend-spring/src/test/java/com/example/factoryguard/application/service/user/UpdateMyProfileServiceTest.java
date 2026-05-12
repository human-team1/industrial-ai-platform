package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UpdateMyProfileCommand;
import com.example.factoryguard.application.dto.user.UserMeResult;
import com.example.factoryguard.application.port.out.organization.FindOrganizationByIdPort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.SaveUserPort;
import com.example.factoryguard.application.service.auth.SessionValidationService;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.organization.model.Organization;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserRole;
import com.example.factoryguard.domain.user.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateMyProfileServiceTest {

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "session-1";
    private static final Long ORG_ID = 100L;

    @Mock FindUserByIdPort findUserByIdPort;
    @Mock SaveUserPort saveUserPort;
    @Mock FindOrganizationByIdPort findOrganizationByIdPort;
    @Mock SessionValidationService sessionValidationService;

    UpdateMyProfileService service;

    @BeforeEach
    void setUp() {
        service = new UpdateMyProfileService(
                findUserByIdPort, saveUserPort, findOrganizationByIdPort, sessionValidationService);
    }

    @Test
    @DisplayName("AUTH-003-02 이름·전화번호 수정 - 정상 저장 + 갱신된 UserMeResult 반환")
    void updatesNameAndPhone() {
        prepareActiveUser("기존이름", "010-1111-2222");
        prepareOrganization();
        when(saveUserPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateMyProfileCommand command = new UpdateMyProfileCommand(
                USER_ID, SESSION_ID, "새이름", "010-9999-8888", null);

        UserMeResult result = service.execute(command);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(saveUserPort).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("새이름");
        assertThat(saved.getPhone()).isEqualTo("010-9999-8888");
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getName()).isEqualTo("새이름");
        assertThat(result.getPhone()).isEqualTo("010-9999-8888");
        assertThat(result.getOrganizationName()).isEqualTo("Acme");
    }

    @Test
    @DisplayName("AUTH-003-02 일부 필드만 PATCH - 누락 필드는 기존 값 유지")
    void preservesUnsetFields() {
        prepareActiveUser("기존이름", "010-1111-2222");
        prepareOrganization();
        when(saveUserPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateMyProfileCommand command = new UpdateMyProfileCommand(
                USER_ID, SESSION_ID, "새이름", null, null);

        service.execute(command);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(saveUserPort).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("새이름");
        assertThat(saved.getPhone()).isEqualTo("010-1111-2222");
    }

    @Test
    @DisplayName("AUTH-003-02 빈 이름 - VALIDATION_FAILED")
    void rejectsBlankName() {
        prepareActiveUser("기존", "010-1111-2222");
        UpdateMyProfileCommand command = new UpdateMyProfileCommand(
                USER_ID, SESSION_ID, "   ", null, null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);
        verify(saveUserPort, never()).save(any());
    }

    @Test
    @DisplayName("AUTH-003-02 잘못된 전화번호 형식 - VALIDATION_FAILED")
    void rejectsInvalidPhoneFormat() {
        prepareActiveUser("기존", "010-1111-2222");
        UpdateMyProfileCommand command = new UpdateMyProfileCommand(
                USER_ID, SESSION_ID, null, "abc전화번호", null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);
        verify(saveUserPort, never()).save(any());
    }

    @Test
    @DisplayName("AUTH-003-02 PENDING 사용자 - PENDING_APPROVAL")
    void blocksPendingUser() {
        when(findUserByIdPort.findById(USER_ID)).thenReturn(Optional.of(buildUser(UserStatus.PENDING)));
        UpdateMyProfileCommand command = new UpdateMyProfileCommand(
                USER_ID, SESSION_ID, "새이름", null, null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PENDING_APPROVAL);
        verify(saveUserPort, never()).save(any());
    }

    private void prepareActiveUser(String name, String phone) {
        when(findUserByIdPort.findById(USER_ID)).thenReturn(Optional.of(
                User.builder()
                        .userId(USER_ID)
                        .organizationId(ORG_ID)
                        .email("user@example.com")
                        .name(name)
                        .phone(phone)
                        .role(UserRole.ROLE_COMPANY_WORKER)
                        .status(UserStatus.ACTIVE)
                        .build()
        ));
    }

    private User buildUser(UserStatus status) {
        return User.builder()
                .userId(USER_ID)
                .organizationId(ORG_ID)
                .email("user@example.com")
                .name("기존")
                .role(UserRole.ROLE_COMPANY_WORKER)
                .status(status)
                .build();
    }

    private void prepareOrganization() {
        lenient().when(findOrganizationByIdPort.findById(ORG_ID)).thenReturn(Optional.of(
                Organization.builder()
                        .organizationId(ORG_ID)
                        .organizationName("Acme")
                        .build()
        ));
    }
}
