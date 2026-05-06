package com.example.factoryguard.config.security;

import com.example.factoryguard.domain.user.model.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        boolean modelManagementRequest = isModelManagementRequest(request);
        String token = extractBearerToken(request);

        if (token == null) {
            if (modelManagementRequest) {
                log.warn("Model management JWT missing or malformed, method={}, path={}, hasAuthorizationHeader={}",
                        request.getMethod(), request.getRequestURI(), request.getHeader(HttpHeaders.AUTHORIZATION) != null);
            }
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        String validationFailure = jwtTokenProvider.getValidationFailureReason(token);
        if (validationFailure == null) {
            Long userId = jwtTokenProvider.extractUserId(token);
            String role = jwtTokenProvider.extractRole(token);
            Long organizationId = jwtTokenProvider.extractOrganizationId(token);
            String sessionId = jwtTokenProvider.extractSessionId(token);

            UserRole verifiedRole = parseRole(role);
            if (verifiedRole == null) {
                log.warn("Reject JWT with unknown or missing role claim, method={}, path={}, userId={}, rawRole={}",
                        request.getMethod(), request.getRequestURI(), userId, role);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                    userId, verifiedRole.name(), organizationId, sessionId);
            var authorities = List.of(new SimpleGrantedAuthority(verifiedRole.name()));
            var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            if (modelManagementRequest) {
                log.info("Model management JWT authenticated, method={}, path={}, userId={}, organizationId={}, sessionId={}, role={}",
                        request.getMethod(), request.getRequestURI(), userId, organizationId, sessionId, verifiedRole.name());
            }
        } else {
            if (modelManagementRequest) {
                log.warn("Model management JWT rejected, method={}, path={}, reason={}",
                        request.getMethod(), request.getRequestURI(), validationFailure);
            }
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private UserRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            return UserRole.valueOf(role.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean isModelManagementRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/models/")
                || path.equals("/api/v1/models")
                || path.startsWith("/api/v1/model-versions/")
                || path.startsWith("/api/v1/model-deployments/");
    }
}
