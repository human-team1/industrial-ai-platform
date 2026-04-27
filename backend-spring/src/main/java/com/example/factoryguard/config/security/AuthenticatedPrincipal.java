package com.example.factoryguard.config.security;

public record AuthenticatedPrincipal(Long userId, String role, Long organizationId, String sessionId) {
}
