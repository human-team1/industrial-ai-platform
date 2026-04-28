package com.example.factoryguard.application.port.out.auth;

import com.example.factoryguard.domain.user.model.AuthSession;

public interface SaveAuthSessionPort {

    AuthSession save(AuthSession authSession);
}
