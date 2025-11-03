package com.eod.eod.domain.auth.application;

import com.eod.eod.common.util.CookieUtil;
import com.eod.eod.domain.auth.presentation.dto.OAuth2LoginResponse;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // OAuth2 제공자 정보
        String provider = extractProvider(request);
        String providerId = oAuth2User.getAttribute("sub"); // Google의 경우 'sub' 클레임
        String email = oAuth2User.getAttribute("email");

        log.info("OAuth2 인증 성공 - provider: {}, providerId: {}, email: {}", provider, providerId, email);

        // 사용자 조회 (provider + providerId 기준)
        User user = userRepository.findByOauthProviderAndOauthId(provider, providerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // JWT 토큰 생성 및 DB 저장
        AuthService.TokenPair tokenPair = authService.issueTokensForOAuth2Login(user);

        // Refresh Token을 HttpOnly Cookie에 저장 (XSS 방어)
        CookieUtil.addSecureCookie(response, "refreshToken", tokenPair.getRefreshToken(), 
                tokenService.getRefreshTokenExpirationSeconds());

        // JSON 응답 생성 (Refresh Token은 Cookie로만 전달, Body에는 포함하지 않음)
        OAuth2LoginResponse loginResponse = OAuth2LoginResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .tokenType("bearer")
                .user(OAuth2LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .build())
                .message("로그인 성공")
                .build();

        // JSON 응답 반환
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(loginResponse));

        log.info("OAuth2 로그인 성공 - userId: {}, JSON 응답 완료", user.getId());
    }

    private String extractProvider(HttpServletRequest request) {
        // URL에서 provider 추출: /auth/oauth/google/callback -> "google"
        String uri = request.getRequestURI();
        if (uri.contains("/google/")) {
            return "google";
        }
        // 다른 provider 추가 시 확장 가능
        return "google"; // 기본값
    }
}
