package com.example.factoryguard.adapter.out.google;

import com.example.factoryguard.application.dto.auth.GoogleTokenInfo;
import com.example.factoryguard.application.port.out.auth.VerifyGoogleTokenPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.OAuth2ClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class GoogleTokenVerificationAdapter implements VerifyGoogleTokenPort {

    private static final String GOOGLE_JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";

    private final String clientId;
    private JwtDecoder jwtDecoder;

    public GoogleTokenVerificationAdapter(OAuth2ClientProperties properties) {
        this.clientId = properties.getClientId();
    }

    @PostConstruct
    public void init() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS_URI).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                token -> {
                    List<String> audiences = token.getAudience();
                    if (audiences != null && audiences.contains(clientId)) {
                        return OAuth2TokenValidatorResult.success();
                    }
                    return OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Invalid audience", null));
                }
        ));
        this.jwtDecoder = decoder;
    }

    @Override
    public GoogleTokenInfo verify(String idToken) {
        try {
            Jwt jwt = jwtDecoder.decode(idToken);
            return GoogleTokenInfo.builder()
                    .sub(jwt.getSubject())
                    .email(jwt.getClaimAsString("email"))
                    .name(jwt.getClaimAsString("name"))
                    .picture(jwt.getClaimAsString("picture"))
                    .build();
        } catch (JwtException e) {
            log.warn("Google ID token verification failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }
    }
}
