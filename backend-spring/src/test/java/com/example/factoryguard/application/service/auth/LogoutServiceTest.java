package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        verify(tokenStorePort, never()).deleteAllRefreshTokensByUserId(eq(userId));
        verify(tokenStorePort, never()).deleteAllSessionsByUserId(eq(userId));
    }

    @Test
    @DisplayName("No.5-2 sessionId 미지정 로그아웃 - 사용자 단위 RefreshToken/Session 일괄 삭제 및 활성 세션 인덱스 정리")
    void invalidatesAllUserSessionsWhenSessionIdMissing() {
        long userId = 1L;
        when(tokenStorePort.findAllSessionIdsByUserId(userId))
                .thenReturn(List.of("sess-001", "sess-002"));

        logoutService.execute(userId, null);

        verify(tokenStorePort).findAllSessionIdsByUserId(userId);
        verify(tokenStorePort).deleteAllRefreshTokensByUserId(userId);
        verify(tokenStorePort).deleteAllSessionsByUserId(userId);
        verify(activeSessionService).removeSession("sess-001");
        verify(activeSessionService).removeSession("sess-002");
        verify(tokenStorePort, never()).deleteRefreshToken(eq(userId), anyString());
        verify(tokenStorePort, never()).deleteSessionId(eq(userId), anyString());
    }

    @Test
    @DisplayName("No.5-3 sessionId 공백 로그아웃 - 사용자 단위 일괄 삭제로 위임")
    void invalidatesAllUserSessionsWhenSessionIdBlank() {
        long userId = 1L;
        when(tokenStorePort.findAllSessionIdsByUserId(userId))
                .thenReturn(List.of());

        logoutService.execute(userId, "   ");

        verify(tokenStorePort).deleteAllRefreshTokensByUserId(userId);
        verify(tokenStorePort).deleteAllSessionsByUserId(userId);
        verify(activeSessionService, never()).removeSession(anyString());
    }

    @Test
    @DisplayName("No.5-4 이미 무효화된 사용자 재요청 - 멱등 처리 (예외 없이 통과)")
    void idempotentWhenNoActiveSessionsRemain() {
        long userId = 1L;
        when(tokenStorePort.findAllSessionIdsByUserId(userId))
                .thenReturn(List.of());

        logoutService.execute(userId, null);

        verify(tokenStorePort).deleteAllRefreshTokensByUserId(userId);
        verify(tokenStorePort).deleteAllSessionsByUserId(userId);
        verify(activeSessionService, never()).removeSession(anyString());
    }
}
