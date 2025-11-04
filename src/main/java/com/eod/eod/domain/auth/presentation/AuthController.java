package com.eod.eod.domain.auth.presentation;

import com.eod.eod.common.util.CookieUtil;
import com.eod.eod.domain.auth.application.AuthService;
import com.eod.eod.domain.auth.application.TokenService;
import com.eod.eod.domain.auth.presentation.dto.TokenResponse;
import com.eod.eod.domain.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;

    @PostMapping("/refresh")
    @Operation(summary = "Access Token 및 Refresh Token 갱신", description = "Cookie의 Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.")
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // Cookie에서 Refresh Token 추출
        String refreshToken = cookieUtil.getCookie(request, "refreshToken")
                .map(Cookie::getValue)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token이 없습니다."));

        // Access Token 및 Refresh Token 갱신
        AuthService.RefreshTokenResult result = authService.refreshAccessToken(refreshToken);

        // 새로운 Refresh Token을 Cookie에 저장
        cookieUtil.addTokenCookie(
                response,
                "refreshToken",
                result.getRefreshToken(),
                tokenService.getRefreshTokenExpirationMillis(),
                CookieUtil.SameSitePolicy.NONE
        );

        // Response DTO 생성
        TokenResponse tokenResponse = TokenResponse.of(result.getAccessToken(), "Bearer");

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하여 로그아웃합니다.")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) {

        // DB에서 Refresh Token 삭제
        authService.logout(user.getId());

        // Cookie에서 Refresh Token 삭제
        cookieUtil.deleteCookie(response, "refreshToken", CookieUtil.SameSitePolicy.NONE);

        return ResponseEntity.ok().build();
    }
}
