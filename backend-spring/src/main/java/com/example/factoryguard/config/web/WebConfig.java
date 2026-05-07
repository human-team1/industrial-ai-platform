package com.example.factoryguard.config.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class WebConfig {

    /**
     * allowedOriginPatterns 는 와일드카드 패턴(https://*.ngrok-free.app 등)과
     * 정확한 origin(http://localhost) 모두 허용하며,
     * allowCredentials(true)와 함께 사용 가능합니다.
     * 운영 프로필(prod)에서는 application-prod.yml 에 엄격한 origin 목록을 명시하세요.
     */
    @Value("${app.cors.allowed-origin-patterns:http://localhost:5173,http://localhost}")
    private List<String> allowedOriginPatterns;

    @Value("${app.cors.exposed-headers:Authorization,X-Request-Id}")
    private List<String> exposedHeaders;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOriginPatterns);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(exposedHeaders);
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
