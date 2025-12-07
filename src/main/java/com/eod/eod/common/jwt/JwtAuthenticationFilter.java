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

            // 토큰 검증
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
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

                    // Principal을 User 객체로 설정 (중요!)
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("JWT 인증 성공 - userId: {}, userName: {}, role: {}",
                              user.getId(), user.getName(), role);
                }
            }
        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
            // 인증 실패 시 SecurityContext 비우기
            SecurityContextHolder.clearContext();
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
