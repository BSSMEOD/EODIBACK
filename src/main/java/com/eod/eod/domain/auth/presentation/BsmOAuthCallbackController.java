package com.eod.eod.domain.auth.presentation;

import com.eod.eod.common.util.CookieUtil;
import com.eod.eod.domain.auth.application.BsmLoginService;
import com.eod.eod.domain.auth.application.DiscordOAuthStateService;
import com.eod.eod.domain.auth.application.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "BSM OAuth", description = "BSM OAuth 콜백")
public class BsmOAuthCallbackController {

    private static final String STATE_COOKIE_NAME = "bsm_oauth_state";
    static final String DISCORD_ID_COOKIE_NAME = "bsm_discord_id";
    private static final String DISCORD_FRONTEND_BASE_URL = "https://eodi.kro.kr";

    private final BsmLoginService bsmLoginService;
    private final DiscordOAuthStateService discordOAuthStateService;
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;
    private final Environment environment;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${frontend.oauth-callback-path}")
    private String frontendCallbackPath;

    @GetMapping("/oauth/bsm")
    @Operation(summary = "BSM OAuth 콜백", description = "BSM에서 전달된 code를 교환해 JWT를 발급하고 프론트로 리다이렉트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "로그인 성공 - 프론트엔드로 Access Token과 함께 리다이렉트"),
            @ApiResponse(responseCode = "302", description = "로그인 실패 - 프론트엔드로 에러 정보와 함께 리다이렉트")
    })
    public void callback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            String discordIdFromState = discordOAuthStateService.consumeDiscordId(state).orElse(null);
            String discordIdFromCookie = cookieUtil.getCookie(request, DISCORD_ID_COOKIE_NAME)
                    .map(Cookie::getValue)
                    .filter(value -> !value.isBlank())
                    .orElse(null);
            String discordId = discordIdFromState != null ? discordIdFromState : discordIdFromCookie;
            log.info("[BSM Callback] discordId resolution: fromState={}, fromCookie={}, resolved={}",
                    discordIdFromState, discordIdFromCookie, discordId);
            cookieUtil.deleteCookie(response, DISCORD_ID_COOKIE_NAME, CookieUtil.SameSitePolicy.LAX);

            String expectedState = cookieUtil.getCookie(request, STATE_COOKIE_NAME)
                    .map(c -> c.getValue())
                    .orElse(null);

            // Discord 봇 플로우가 아니면 기존 쿠키 기반 state를 유지한다.
            if (discordId == null && (expectedState == null || state == null || !expectedState.equals(state))) {
                log.warn("BSM OAuth state mismatch. expected={}, actual={} (continuing login process)", expectedState, state);
            }

            // State 쿠키 삭제 (있는 경우)
            if (expectedState != null) {
                cookieUtil.deleteCookie(response, STATE_COOKIE_NAME, CookieUtil.SameSitePolicy.LAX);
            }

            BsmLoginService.LoginResult loginResult = bsmLoginService.login(code);

            String discordLinkError = null;
            if (discordId != null) {
                log.info("[BSM Callback] linkDiscordId 진입 userId={} currentDiscordId={} newDiscordId={}",
                        loginResult.user().getId(), loginResult.user().getDiscordId(), discordId);
                try {
                    bsmLoginService.linkDiscordId(loginResult.user(), discordId);
                    log.info("[BSM Callback] linkDiscordId 완료 userId={}", loginResult.user().getId());
                } catch (IllegalStateException e) {
                    log.warn("Discord ID 연결 실패 (BSM 로그인은 성공): {}", e.getMessage());
                    discordLinkError = e.getMessage();
                } catch (Exception e) {
                    log.error("Discord ID 연결 중 예외 발생 (BSM 로그인은 성공)", e);
                    discordLinkError = e.getClass().getSimpleName() + ": " + e.getMessage();
                }
            } else {
                log.info("[BSM Callback] discordId is null, skipping linkDiscordId");
            }

            cookieUtil.addTokenCookie(
                    response,
                    "refreshToken",
                    loginResult.refreshToken(),
                    tokenService.getRefreshTokenExpirationMillis(),
                    CookieUtil.SameSitePolicy.NONE
            );

            // Google OAuth2와 동일하게 access token을 fragment로 전달
            // Discord 연결 실패 시에도 BSM 로그인은 유지하고 에러 정보를 함께 전달
            String redirectBaseUrl = resolveRedirectBaseUrl(discordId);
            String redirectUrl = discordLinkError != null
                    ? String.format(
                            "%s%s#token=%s&provider=bsm&discord_error=%s",
                            redirectBaseUrl,
                            frontendCallbackPath,
                            loginResult.accessToken(),
                            URLEncoder.encode(discordLinkError, StandardCharsets.UTF_8))
                    : String.format(
                            "%s%s#token=%s&provider=bsm",
                            redirectBaseUrl,
                            frontendCallbackPath,
                            loginResult.accessToken());
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("BSM OAuth callback failed", e);
            cookieUtil.deleteCookie(response, STATE_COOKIE_NAME, CookieUtil.SameSitePolicy.LAX);
            cookieUtil.deleteCookie(response, DISCORD_ID_COOKIE_NAME, CookieUtil.SameSitePolicy.LAX);
            failRedirect(response, "bsm_oauth_failed", null);
        }
    }

    private void failRedirect(HttpServletResponse response, String reason, String discordId) throws IOException {
        String redirectUrl = String.format(
                "%s%s#error=%s&message=%s",
                resolveRedirectBaseUrl(discordId),
                frontendCallbackPath,
                URLEncoder.encode(reason, StandardCharsets.UTF_8),
                URLEncoder.encode("BSM 로그인에 실패했습니다.", StandardCharsets.UTF_8)
        );
        response.sendRedirect(redirectUrl);
    }

    private String resolveRedirectBaseUrl(String discordId) {
        if (discordId != null && !discordId.isBlank()) {
            return DISCORD_FRONTEND_BASE_URL;
        }

        if (shouldFallbackToDiscordFrontend(frontendBaseUrl)) {
            log.warn("Unsafe frontend.base-url detected for BSM callback: {}. Falling back to {}",
                    frontendBaseUrl, DISCORD_FRONTEND_BASE_URL);
            return DISCORD_FRONTEND_BASE_URL;
        }

        return frontendBaseUrl;
    }

    private boolean shouldFallbackToDiscordFrontend(String baseUrl) {
        if (isTestProfile()) {
            return false;
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            return true;
        }

        String normalized = baseUrl.toLowerCase(Locale.ROOT);
        return normalized.contains("localhost") || normalized.contains("127.0.0.1");
    }

    private boolean isTestProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("test"));
    }
}
