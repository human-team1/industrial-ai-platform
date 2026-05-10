package com.example.factoryguard.config.security;

import com.example.factoryguard.domain.user.model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        properties.setExpireMinutes(30);
        properties.setRefreshExpireDays(7);
        jwtTokenProvider = new JwtTokenProvider(properties);
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("No.4 일반 사용자 토큰 - GrantedAuthority가 ROLE_COMPANY_WORKER이며 SITE_ADMIN 권한 없음")
    void workerTokenHasNoSiteAdminAuthority() throws Exception {
        String workerToken = jwtTokenProvider.generateAccessToken(1L, UserRole.ROLE_COMPANY_WORKER, 10L, "sess-w");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + workerToken);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        var authorityNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertThat(authorityNames).containsExactly("ROLE_COMPANY_WORKER");
        assertThat(authorityNames).doesNotContain("ROLE_SITE_ADMIN");
    }

    @Test
    @DisplayName("No.4-2 SITE_ADMIN 토큰 - SITE_ADMIN 권한 부여, 일반 권한과 분리")
    void siteAdminTokenHasSiteAdminAuthority() throws Exception {
        String adminToken = jwtTokenProvider.generateAccessToken(2L, UserRole.ROLE_SITE_ADMIN, 10L, "sess-a");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + adminToken);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        var authorityNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertThat(authorityNames).containsExactly("ROLE_SITE_ADMIN");
        assertThat(authorityNames).doesNotContain("ROLE_COMPANY_WORKER");
    }

    @Test
    @DisplayName("No.4-3 토큰 없음 - SecurityContext 비어 있음")
    void noTokenLeavesContextEmpty() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("만료된 JWT는 실패 사유를 JWT_EXPIRED로 구분한다")
    void expiredTokenHasValidationFailureReason() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        JwtTokenProvider provider = new JwtTokenProvider(properties);

        java.util.Date now = new java.util.Date();
        String expired = io.jsonwebtoken.Jwts.builder()
                .setSubject("3")
                .claim("role", UserRole.ROLE_SITE_ADMIN.name())
                .claim("orgId", 10L)
                .claim("sid", "sess-expired")
                .setIssuedAt(new java.util.Date(now.getTime() - 120_000L))
                .setExpiration(new java.util.Date(now.getTime() - 60_000L))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                                properties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                        io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        assertThat(provider.validateToken(expired)).isFalse();
        assertThat(provider.getValidationFailureReason(expired)).isEqualTo("JWT_EXPIRED");
    }

    @Test
    @DisplayName("No.4-4 위조 role(예: ROLE_HACKER) - SecurityContext 비움")
    void rejectsForgedRoleClaim() throws Exception {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        properties.setExpireMinutes(30);
        JwtTokenProvider provider = new JwtTokenProvider(properties);

        java.util.Date now = new java.util.Date();
        java.util.Date expiry = new java.util.Date(now.getTime() + 60_000L);
        String forged = io.jsonwebtoken.Jwts.builder()
                .setSubject("3")
                .claim("role", "ROLE_HACKER")
                .claim("orgId", 10L)
                .claim("sid", "sess-x")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        properties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                        io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + forged);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("No.4-5 빈 role claim - SecurityContext 비움")
    void rejectsEmptyRoleClaim() throws Exception {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        JwtTokenProvider provider = new JwtTokenProvider(properties);

        java.util.Date now = new java.util.Date();
        java.util.Date expiry = new java.util.Date(now.getTime() + 60_000L);
        String empty = io.jsonwebtoken.Jwts.builder()
                .setSubject("3")
                .claim("role", "")
                .claim("orgId", 10L)
                .claim("sid", "sess-y")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        properties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                        io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + empty);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
