package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.port.in.auth.LogoutUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final TokenStorePort tokenStorePort;

    @Override
    public void execute(Long userId) {
        tokenStorePort.deleteRefreshToken(userId);
    }
}
