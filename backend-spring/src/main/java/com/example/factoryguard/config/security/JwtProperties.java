package com.example.factoryguard.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long expireMinutes;
    // 리프레시 토큰 기본 7일
    private long refreshExpireDays = 7;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireMinutes() {
        return expireMinutes;
    }

    public void setExpireMinutes(long expireMinutes) {
        this.expireMinutes = expireMinutes;
    }

    public long getRefreshExpireDays() {
        return refreshExpireDays;
    }

    public void setRefreshExpireDays(long refreshExpireDays) {
        this.refreshExpireDays = refreshExpireDays;
    }
}
