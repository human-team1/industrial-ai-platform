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
        String token = extractBearerToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.extractUserId(token);
            String role = jwtTokenProvider.extractRole(token);
            Long organizationId = jwtTokenProvider.extractOrganizationId(token);
            String sessionId = jwtTokenProvider.extractSessionId(token);

            UserRole verifiedRole = parseRole(role);
            if (verifiedRole == null) {
                log.warn("Reject JWT with unknown or missing role claim, userId={}, rawRole={}", userId, role);
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
        } else {
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
}
