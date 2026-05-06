package com.eod.eod.domain.auth.presentation;

import com.eod.eod.common.util.CookieUtil;
import com.eod.eod.domain.auth.application.BsmOAuthService;
import com.eod.eod.domain.auth.application.DiscordOAuthStateService;
import com.eod.eod.domain.auth.presentation.dto.response.BsmAuthorizeUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Base64;

@RestController
@RequestMapping("/auth/oauth/bsm")
@RequiredArgsConstructor
@Tag(name = "BSM OAuth", description = "BSM OAuth 시작 API")
public class BsmOAuthController {

    private final BsmOAuthService bsmOAuthService;
    private final DiscordOAuthStateService discordOAuthStateService;
    private final CookieUtil cookieUtil;

    private static final String STATE_COOKIE_NAME = "bsm_oauth_state";
    private static final long STATE_COOKIE_MAX_AGE_MILLIS = 5 * 60 * 1000L; // 5 minutes
    private static final long DISCORD_ID_COOKIE_MAX_AGE_MILLIS = 5 * 60 * 1000L;
    private final SecureRandom secureRandom = new SecureRandom();

    @GetMapping("/authorize")
    @Operation(summary = "BSM 로그인 시작", description = "state를 설정하고 BSM 로그인 페이지로 리다이렉트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "BSM 로그인 페이지로 리다이렉트")
    })
    public void authorize(HttpServletResponse response) {
        String state = generateState();
        cookieUtil.addTokenCookie(response, STATE_COOKIE_NAME, state, STATE_COOKIE_MAX_AGE_MILLIS, CookieUtil.SameSitePolicy.LAX);

        String authorizeUrl = bsmOAuthService.buildAuthorizeUrl(state);
        response.setStatus(302);
        response.setHeader("Location", authorizeUrl);
    }

    @GetMapping("/authorize/discord")
    @Operation(summary = "Discord 봇용 BSM 로그인 URL 발급", description = "state와 Discord ID를 서버에 저장하고 fallback 쿠키와 함께 OAuth URL을 JSON으로 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OAuth URL 반환")
    })
    public ResponseEntity<BsmAuthorizeUrlResponse> authorizeForDiscord(
            @RequestParam("discordId") String discordId,
            HttpServletResponse response) {
        String state = generateState();
        discordOAuthStateService.save(state, discordId);
        cookieUtil.addTokenCookie(
                response,
                BsmOAuthCallbackController.DISCORD_ID_COOKIE_NAME,
                discordId,
                DISCORD_ID_COOKIE_MAX_AGE_MILLIS,
                CookieUtil.SameSitePolicy.LAX
        );
        String authorizeUrl = bsmOAuthService.buildAuthorizeUrl(state);
        return ResponseEntity.ok(new BsmAuthorizeUrlResponse(authorizeUrl));
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
