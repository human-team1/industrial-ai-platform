package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.dto.auth.AuthStatus;
import com.example.factoryguard.application.dto.auth.GoogleLoginCommand;
import com.example.factoryguard.application.dto.auth.GoogleLoginResult;
import com.example.factoryguard.application.dto.auth.GoogleTokenInfo;
import com.example.factoryguard.application.port.in.operation.RecordOperationLogUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.auth.VerifyGoogleTokenPort;
import com.example.factoryguard.application.port.out.user.FindUserByGoogleSubPort;
import com.example.factoryguard.config.security.JwtProperties;
import com.example.factoryguard.config.security.JwtTokenProvider;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserRole;
import com.example.factoryguard.domain.user.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleLoginServiceTest {

    @Mock
    private VerifyGoogleTokenPort verifyGoogleTokenPort;
    @Mock
    private FindUserByGoogleSubPort findUserByGoogleSubPort;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenStorePort tokenStorePort;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private RecordOperationLogUseCase recordOperationLogUseCase;
    @Mock
    private ActiveSessionService activeSessionService;

    @InjectMocks
    private GoogleLoginService googleLoginService;

    private GoogleTokenInfo tokenInfo;

    @BeforeEach
    void setUp() {
        tokenInfo = GoogleTokenInfo.builder()
                .sub("google-sub-001")
                .email("user@test.com")
                .name("홍길동")
                .picture("https://pic")
                .build();
        lenient().when(verifyGoogleTokenPort.verify(anyString())).thenReturn(tokenInfo);
        lenient().when(jwtProperties.getRefreshExpireDays()).thenReturn(7L);
    }

    @Test
    @DisplayName("No.1 로그인 사용자 조회 - googleSub 기준으로 사용자 객체를 조회한다")
    void findsUserByGoogleSub() {
        User user = baseUser(UserStatus.ACTIVE);
        when(findUserByGoogleSubPort.findByGoogleSub("google-sub-001")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(anyLong(), any(), anyLong(), anyString())).thenReturn("AT");
        when(jwtTokenProvider.generateRefreshToken(anyLong(), any(), anyLong(), anyString())).thenReturn("RT");

        GoogleLoginResult result = googleLoginService.execute(new GoogleLoginCommand("id-token"));

        verify(findUserByGoogleSubPort).findByGoogleSub("google-sub-001");
        assertThat(result.getEmail()).isEqualTo("user@test.com");
        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("No.2 PENDING 사용자 - 토큰 미발급, AuthStatus=PENDING 안내 반환")
    void blocksPendingUser() {
        User user = baseUser(UserStatus.PENDING);
        when(findUserByGoogleSubPort.findByGoogleSub(anyString())).thenReturn(Optional.of(user));

        GoogleLoginResult result = googleLoginService.execute(new GoogleLoginCommand("id-token"));

        assertThat(result.getUserStatus()).isEqualTo(AuthStatus.PENDING);
        assertThat(result.getAccessToken()).isNull();
        assertThat(result.getRefreshToken()).isNull();
        verify(jwtTokenProvider, never()).generateAccessToken(anyLong(), any(), anyLong(), anyString());
        verify(activeSessionService, never()).registerSession(anyLong(), anyString());
    }

    @Test
    @DisplayName("No.2-2 REJECTED 사용자 - 토큰 미발급, AuthStatus=REJECTED 안내 반환")
    void blocksRejectedUser() {
        User user = baseUser(UserStatus.REJECTED);
        when(findUserByGoogleSubPort.findByGoogleSub(anyString())).thenReturn(Optional.of(user));

        GoogleLoginResult result = googleLoginService.execute(new GoogleLoginCommand("id-token"));

        assertThat(result.getUserStatus()).isEqualTo(AuthStatus.REJECTED);
        assertThat(result.getAccessToken()).isNull();
    }

    @Test
    @DisplayName("No.3 ACTIVE 사용자 - AccessToken/RefreshToken 발급 및 세션 등록")
    void activeUserGetsTokens() {
        User user = baseUser(UserStatus.ACTIVE);
        when(findUserByGoogleSubPort.findByGoogleSub(anyString())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(eq(1L), any(), eq(10L), anyString())).thenReturn("AT");
        when(jwtTokenProvider.generateRefreshToken(eq(1L), any(), eq(10L), anyString())).thenReturn("RT");

        GoogleLoginResult result = googleLoginService.execute(new GoogleLoginCommand("id-token"));

        assertThat(result.getUserStatus()).isEqualTo(AuthStatus.ACTIVE);
        assertThat(result.getAccessToken()).isEqualTo("AT");
        assertThat(result.getRefreshToken()).isEqualTo("RT");
        verify(activeSessionService).assertCanRegisterNewSession();
        verify(tokenStorePort).saveRefreshToken(eq(1L), anyString(), eq("RT"), any(Duration.class));
        verify(tokenStorePort).saveSessionId(eq(1L), anyString(), any(Duration.class));
        verify(activeSessionService).registerSession(eq(1L), anyString());
    }

    private User baseUser(UserStatus status) {
        return User.builder()
                .userId(1L)
                .organizationId(10L)
                .googleSub("google-sub-001")
                .email("user@test.com")
                .name("홍길동")
                .status(status)
                .role(UserRole.ROLE_COMPANY_WORKER)
                .build();
    }
}
