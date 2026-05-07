package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class SessionValidationServiceTest {

    @Mock TokenStorePort tokenStorePort;
    @Mock ActiveSessionService activeSessionService;

    SessionValidationService service;

    @BeforeEach
    void setUp() {
        service = new SessionValidationService(tokenStorePort, activeSessionService);
    }

    @Test
    void redisFailureIsMappedToSessionStoreUnavailable() {
        doThrow(new RuntimeException("redis down"))
                .when(tokenStorePort).hasSessionId(1L, "session-1");

        assertThatThrownBy(() -> service.validate(1L, "session-1"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SESSION_STORE_UNAVAILABLE));
    }
}
