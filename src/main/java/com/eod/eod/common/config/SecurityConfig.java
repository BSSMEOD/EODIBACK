package com.eod.eod.common.config;

import com.eod.eod.common.jwt.JwtAuthenticationFilter;
import com.eod.eod.domain.auth.application.CustomOAuth2UserService;
import com.eod.eod.domain.auth.application.OAuth2SuccessHandler;
import com.eod.eod.domain.auth.application.OAuth2FailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 보안 헤더 설정
                .headers(headers -> headers
                        // X-Frame-Options: Clickjacking 방어
                        .frameOptions(frame -> frame.deny())
                        // X-Content-Type-Options: MIME 타입 스니핑 방지 (nosniff)
                        .contentTypeOptions(Customizer.withDefaults())
                        // Content-Security-Policy: 외부 리소스 출처 제한
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:; " +
                                        "font-src 'self' data:; " +
                                        "connect-src 'self' https://accounts.google.com; " +
                                        "frame-ancestors 'none'; " +
                                        "base-uri 'self'; " +
                                        "form-action 'self'")
                        )
                        // Referrer-Policy: 민감 정보 전파 차단
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
                        )
                        // Strict-Transport-Security: HTTPS 강제 (1년 유지)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                        // Permissions-Policy: 민감한 브라우저 기능 제한
                        .addHeaderWriter(new StaticHeadersWriter(
                                "Permissions-Policy",
                                "camera=(), microphone=(), geolocation=(), payment=()"
                        ))
                )
                .authorizeHttpRequests(auth -> auth
                        // Swagger 경로 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // OAuth2 로그인 경로 허용
                        .requestMatchers("/login/**", "/oauth2/**", "/auth/oauth/**").permitAll()
                        // Auth API 허용
                        .requestMatchers("/auth/**").permitAll()
                        // 테스트 페이지 허용
                        .requestMatchers("/test/**").permitAll()
                        // 테스트를 위해 임시로 모든 item API 허용
                        .requestMatchers("/items/**").permitAll()
                        // 테스트를 위해 임시로 모든 reward API 허용
                        .requestMatchers("/rewards/**").permitAll()
                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
