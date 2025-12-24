package com.eod.eod.domain.auth.presentation;

import com.eod.eod.common.util.CookieUtil;
import com.eod.eod.domain.auth.application.BsmLoginService;
import com.eod.eod.domain.auth.application.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "BSM OAuth", description = "BSM OAuth 콜백")
public class BsmOAuthCallbackController {

    private static final String STATE_COOKIE_NAME = "bsm_oauth_state";

    private final BsmLoginService bsmLoginService;
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${frontend.oauth-callback-path}")
    private String frontendCallbackPath;

    @GetMapping("/oauth/bsm")
    @Operation(summary = "BSM OAuth 콜백", description = "BSM에서 전달된 code를 교환해 JWT를 발급하고 프론트로 리다이렉트합니다.")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            String expectedState = cookieUtil.getCookie(request, STATE_COOKIE_NAME)
                    .map(c -> c.getValue())
                    .orElse(null);

            // State 검증 (BSM OAuth가 state를 반환하지 않으므로 경고만 처리)
            if (expectedState == null || state == null || !expectedState.equals(state)) {
                log.warn("BSM OAuth state mismatch. expected={}, actual={} (continuing login process)", expectedState, state);
            }

            // State 쿠키 삭제 (있는 경우)
            if (expectedState != null) {
                cookieUtil.deleteCookie(response, STATE_COOKIE_NAME, CookieUtil.SameSitePolicy.LAX);
            }

            BsmLoginService.LoginResult loginResult = bsmLoginService.login(code);

            cookieUtil.addTokenCookie(
                    response,
                    "refreshToken",
                    loginResult.refreshToken(),
                    tokenService.getRefreshTokenExpirationMillis(),
                    CookieUtil.SameSitePolicy.NONE
            );

            // Google OAuth2와 동일하게 access token을 fragment로 전달
            String redirectUrl = String.format(
                    "%s%s#token=%s&provider=bsm",
                    frontendBaseUrl,
                    frontendCallbackPath,
                    loginResult.accessToken()
            );
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("BSM OAuth callback failed", e);
            cookieUtil.deleteCookie(response, STATE_COOKIE_NAME, CookieUtil.SameSitePolicy.LAX);
            failRedirect(response, "bsm_oauth_failed");
        }
    }

    private void failRedirect(HttpServletResponse response, String reason) throws IOException {
        String redirectUrl = String.format(
                "%s%s#error=%s&message=%s",
                frontendBaseUrl,
                frontendCallbackPath,
                URLEncoder.encode(reason, StandardCharsets.UTF_8),
                URLEncoder.encode("BSM 로그인에 실패했습니다.", StandardCharsets.UTF_8)
        );
        response.sendRedirect(redirectUrl);
    }
}
