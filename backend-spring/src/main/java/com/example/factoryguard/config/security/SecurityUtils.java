package com.example.factoryguard.config.security;

import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public AuthenticatedPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AuthenticatedPrincipal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return (AuthenticatedPrincipal) auth.getPrincipal();
    }

    public Long getCurrentUserId() {
        return getCurrentPrincipal().userId();
    }

    public Long getCurrentOrgId() {
        return getCurrentPrincipal().organizationId();
    }
}