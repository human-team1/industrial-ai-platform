package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private TokenStorePort tokenStorePort;
    @Mock
    private ActiveSessionService activeSessionService;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    @DisplayName("No.5 sessionId 지정 로그아웃 - RefreshToken/SessionId 삭제 및 ActiveSession 제거")
    void invalidatesRefreshAndSessionWhenSessionIdProvided() {
        long userId = 1L;
        String sessionId = "sess-001";

        logoutService.execute(userId, sessionId);

        verify(tokenStorePort).deleteRefreshToken(userId, sessionId);
        verify(tokenStorePort).deleteSessionId(userId, sessionId);
        verify(activeSessionService).removeSession(sessionId);
        verify(tokenStorePort, never()).deleteSessionId(eq(userId));
    }

    @Test
    @DisplayName("No.5-2 sessionId 미지정 로그아웃 - 사용자 단위 SessionId 일괄 삭제")
    void invalidatesUserSessionWhenSessionIdMissing() {
        long userId = 1L;

        logoutService.execute(userId, null);

        verify(tokenStorePort).deleteSessionId(userId);
        verify(tokenStorePort, never()).deleteRefreshToken(eq(userId), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("No.5-3 sessionId 공백 로그아웃 - 사용자 단위 SessionId 일괄 삭제")
    void invalidatesUserSessionWhenSessionIdBlank() {
        long userId = 1L;

        logoutService.execute(userId, "   ");

        verify(tokenStorePort).deleteSessionId(userId);
        verify(activeSessionService, never()).removeSession(org.mockito.ArgumentMatchers.anyString());
    }
}
