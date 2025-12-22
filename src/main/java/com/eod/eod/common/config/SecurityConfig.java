package com.eod.eod.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import com.eod.eod.common.jwt.JwtAuthenticationFilter;
import com.eod.eod.domain.auth.application.CustomOAuth2UserService;
import com.eod.eod.domain.auth.application.OAuth2SuccessHandler;
import com.eod.eod.domain.auth.application.OAuth2FailureHandler;
import com.eod.eod.common.exception.CustomAccessDeniedHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // 보안 헤더 설정
                .headers(headers -> headers
                        // X-Frame-Options: Clickjacking 방어
                        .frameOptions(frame -> frame.deny())
                        // X-Content-Type-Options: MIME 타입 스니핑 방지 (nosniff)
                        .contentTypeOptions(Customizer.withDefaults())
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
                        // BSM OAuth callback
                        .requestMatchers("/oauth/bsm").permitAll()
                        // 로그아웃은 인증된 사용자만 가능
                        .requestMatchers(HttpMethod.POST, "/auth/logout").hasAnyRole("USER", "TEACHER", "ADMIN")
                        // 그 외 Auth API 허용 (로그인, 토큰 재발급 등)
                        .requestMatchers("/auth/**").permitAll()
                        // 테스트 페이지 허용
                        .requestMatchers("/test/**").permitAll()

                        // ===== Items API 세분화 =====
                        // 공개 물품 조회 API 허용
                        .requestMatchers(HttpMethod.GET, "/items/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/items/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/items/*/disposal-reason").permitAll()

                        // Claim 관련 API - 학생(USER)만 가능
                        .requestMatchers(HttpMethod.POST, "/items/*/claim").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/items/claims/**").hasRole("ADMIN")

                        // 물품 등록/수정/삭제는 관리자만
                        .requestMatchers(HttpMethod.POST, "/items").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/items/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/items/*").hasRole("ADMIN")

                        // 물품 지급은 관리자만
                        .requestMatchers(HttpMethod.POST, "/items/*/give").hasRole("ADMIN")

                        // 승인/거절은 관리자만
                        .requestMatchers(HttpMethod.PATCH, "/items/*/approval").hasRole("ADMIN")

                        // 나머지 items API는 인증된 사용자 (학생/선생님/관리자 공통)
                        .requestMatchers("/items/**").hasAnyRole("USER", "TEACHER", "ADMIN")

                        // 상점 API는 교사 또는 관리자만 접근
                        .requestMatchers("/rewards/**").hasAnyRole("TEACHER", "ADMIN")
                        // Place-Controller는 모두 허용
                        .requestMatchers("/places/**").permitAll()
                        // 소개 페이지 조회는 공개, 수정은 관리자만
                        .requestMatchers(HttpMethod.PATCH, "/introduce").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/introduce").permitAll()

                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)
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
