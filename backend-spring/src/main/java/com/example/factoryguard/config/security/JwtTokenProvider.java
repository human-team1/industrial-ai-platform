package com.example.factoryguard.config.security;

import com.example.factoryguard.domain.user.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String SIGNUP_TOKEN_TYPE = "SIGNUP";
    private static final long SIGNUP_TOKEN_EXPIRE_MINUTES = 10;

    private final JwtProperties jwtProperties;

    public String generateAccessToken(Long userId, UserRole role, Long organizationId, String sessionId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpireMinutes() * 60 * 1000L);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role.name())
                .claim("orgId", organizationId)
                .claim("sid", sessionId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId, UserRole role, Long organizationId, String sessionId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshExpireDays() * 24 * 60 * 60 * 1000L);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role.name())
                .claim("orgId", organizationId)
                .claim("sid", sessionId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Google OAuth 검증 후 신규 유저에게 발급하는 단기 토큰. googleSub/email/name/picture 포함 (10분 유효). */
    public String generateSignupToken(String googleSub, String email, String name, String picture) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + SIGNUP_TOKEN_EXPIRE_MINUTES * 60 * 1000L);

        return Jwts.builder()
                .setSubject(googleSub)
                .claim("type", SIGNUP_TOKEN_TYPE)
                .claim("email", email)
                .claim("name", name)
                .claim("picture", picture)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * signupToken에서 googleSub 추출. type 클레임이 "SIGNUP"이 아니면 JwtException을 던진다.
     */
    public String extractSignupGoogleSub(String token) {
        return extractSignupClaim(token, Claims::getSubject);
    }

    public String extractSignupEmail(String token) {
        return extractSignupClaim(token, c -> c.get("email", String.class));
    }

    public String extractSignupName(String token) {
        return extractSignupClaim(token, c -> c.get("name", String.class));
    }

    public String extractSignupPicture(String token) {
        return extractSignupClaim(token, c -> c.get("picture", String.class));
    }

    private <T> T extractSignupClaim(String token, java.util.function.Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        if (!SIGNUP_TOKEN_TYPE.equals(claims.get("type", String.class))) {
            throw new JwtException("Not a signup token");
        }
        return resolver.apply(claims);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractAllClaims(token).getSubject());
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Long extractOrganizationId(String token) {
        Object orgId = extractAllClaims(token).get("orgId");
        if (orgId == null) return null;
        return ((Number) orgId).longValue();
    }

    public String extractSessionId(String token) {
        return extractAllClaims(token).get("sid", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}