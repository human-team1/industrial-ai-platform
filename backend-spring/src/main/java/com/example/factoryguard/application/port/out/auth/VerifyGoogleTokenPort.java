package com.example.factoryguard.application.port.out.auth;

import com.example.factoryguard.application.dto.auth.GoogleTokenInfo;

public interface VerifyGoogleTokenPort {

    GoogleTokenInfo verify(String idToken);
}
