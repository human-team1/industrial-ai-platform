package com.example.factoryguard.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ProblemDetailsAuthenticationEntryPoint authenticationEntryPoint;
    private final ProblemDetailsAccessDeniedHandler accessDeniedHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                .antMatchers(
                    "/api/v1/health",
                    "/api/v1/auth/google",
                    "/api/v1/auth/refresh",
                    "/actuator/health"
                ).permitAll()
                .antMatchers(HttpMethod.POST,  "/api/v1/signup-requests").permitAll()
                .antMatchers(HttpMethod.GET,   "/api/v1/signup-requests/organizations/public").permitAll()
                .antMatchers(HttpMethod.GET,   "/api/v1/signup-requests").hasRole("SITE_ADMIN")
                .antMatchers(HttpMethod.PATCH, "/api/v1/signup-requests/*/approve").hasRole("SITE_ADMIN")
                .antMatchers(HttpMethod.PATCH, "/api/v1/signup-requests/*/reject").hasRole("SITE_ADMIN")
                .antMatchers("/api/v1/admin/**", "/api/v1/operations/**").hasRole("SITE_ADMIN")
                .antMatchers("/api/v1/models/**", "/api/v1/model-versions/**", "/api/v1/model-deployments/**", "/api/v1/reviews/**").hasRole("SITE_ADMIN")
                .antMatchers(HttpMethod.GET, "/api/v1/organizations/public").permitAll()
                .anyRequest().authenticated())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
