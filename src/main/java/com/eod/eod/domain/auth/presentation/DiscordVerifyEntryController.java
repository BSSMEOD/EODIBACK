package com.eod.eod.domain.auth.presentation;

import com.eod.eod.common.util.CookieUtil;
import com.eod.eod.domain.auth.application.BsmOAuthService;
import com.eod.eod.domain.auth.application.DiscordOAuthStateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

@RestController
@RequiredArgsConstructor
@Tag(name = "BSM OAuth", description = "Discord 인증 진입점")
public class DiscordVerifyEntryController {

    private static final long DISCORD_ID_COOKIE_MAX_AGE_MILLIS = 5 * 60 * 1000L;
    private final SecureRandom secureRandom = new SecureRandom();

    private final BsmOAuthService bsmOAuthService;
    private final DiscordOAuthStateService discordOAuthStateService;
    private final CookieUtil cookieUtil;

    @GetMapping("/discord-verify")
    @Operation(summary = "Discord 봇 DM 진입점", description = "Discord ID 쿠키를 심고 BSM 로그인 페이지로 302 리다이렉트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "BSM 로그인 페이지로 리다이렉트")
    })
    public void verify(
            @RequestParam("discordId") String discordId,
            HttpServletResponse response
    ) throws IOException {
        String state = generateState();
        discordOAuthStateService.save(state, discordId);
        cookieUtil.addTokenCookie(
                response,
                BsmOAuthCallbackController.DISCORD_ID_COOKIE_NAME,
                discordId,
                DISCORD_ID_COOKIE_MAX_AGE_MILLIS,
                CookieUtil.SameSitePolicy.LAX
        );
        response.sendRedirect(bsmOAuthService.buildAuthorizeUrl(state));
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
