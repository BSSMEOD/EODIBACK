package com.eod.eod.domain.auth.application;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${frontend.oauth-callback-path}")
    private String frontendCallbackPath;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 인증 실패: {}", exception.getMessage());

        // 프론트엔드로 리다이렉트 (에러 정보를 Fragment에 포함)
        String redirectUrl = String.format(
                "%s%s#error=oauth_failed&message=%s",
                frontendBaseUrl,
                frontendCallbackPath,
                java.net.URLEncoder.encode("OAuth 인증에 실패했습니다.", "UTF-8")
        );

        log.info("OAuth2 인증 실패 - 프론트엔드로 리다이렉트: {}", frontendBaseUrl + frontendCallbackPath);

        response.sendRedirect(redirectUrl);
    }
}
