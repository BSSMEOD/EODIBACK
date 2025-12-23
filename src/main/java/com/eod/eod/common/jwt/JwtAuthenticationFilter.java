package com.eod.eod.common.jwt;

import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Request에서 JWT 토큰 추출
            String token = getJwtFromRequest(request);

            // 토큰이 없으면 필터 체인 계속 진행 (공개 엔드포인트 허용)
            if (!StringUtils.hasText(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰이 있으면 검증 시도 (만료 예외 구분 처리)
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    // Access Token인지 확인
                    if ("access".equals(jwtTokenProvider.getTokenType(token))) {
                        Long userId = jwtTokenProvider.getUserIdFromToken(token);
                        String role = jwtTokenProvider.getRoleFromToken(token);

                        // DB에서 User 엔티티 조회
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

                        // Spring Security 인증 객체 생성
                        List<SimpleGrantedAuthority> authorities = List.of(
                                new SimpleGrantedAuthority("ROLE_" + role)
                        );

                        // Principal을 User 객체로 설정
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // SecurityContext에 인증 정보 설정
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.info("JWT 인증 성공 - userId: {}, userName: {}, role: {}, authorities: {}",
                                user.getId(), user.getName(), role, authorities);
                    } else {
                        // Access Token이 아닌 경우 (Refresh Token 등)
                        log.warn("Access Token이 아닌 토큰 유형: {}", jwtTokenProvider.getTokenType(token));
                        sendUnauthorizedResponse(response, "Access Token이 필요합니다.");
                        return;
                    }
                } else {
                    // 토큰이 유효하지 않은 경우 (만료 포함)
                    log.warn("유효하지 않은 JWT 토큰");
                    sendUnauthorizedResponse(response, "유효하지 않은 토큰입니다.");
                    return;
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // 토큰 만료 - 401 반환하고 필터 체인 중단
                log.debug("만료된 JWT 토큰: {}", e.getMessage());
                sendUnauthorizedResponse(response, "토큰이 만료되었습니다.");
                return;
            }

        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
            sendUnauthorizedResponse(response, "인증에 실패했습니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Request Header에서 JWT 토큰 추출
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 401 Unauthorized 응답 전송
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // OAuth2 초기 진입 등 인증이 필요 없는 경로에서는 JWT 검증을 건너뛴다.
        String path = request.getRequestURI();
        return path.startsWith("/oauth2/")
                || path.startsWith("/login/")
                || path.startsWith("/auth/oauth/")
                || path.startsWith("/auth/"); // refresh, logout 등은 자체 로직에서 처리
    }
}
