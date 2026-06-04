package com.eod.eod.domain.auth.presentation;

import com.eod.eod.domain.auth.application.AuthService;
import com.eod.eod.domain.auth.application.BsmOAuthService;
import com.eod.eod.domain.auth.application.MobileAuthTokenService;
import com.eod.eod.domain.auth.application.MobileOAuthStateService;
import com.eod.eod.domain.auth.model.MobileAuthToken;
import com.eod.eod.domain.auth.presentation.dto.request.MobileRefreshRequest;
import com.eod.eod.domain.auth.presentation.dto.request.MobileTokenExchangeRequest;
import com.eod.eod.domain.auth.presentation.dto.response.AuthUserResponse;
import com.eod.eod.domain.auth.presentation.dto.response.BsmAuthorizeUrlResponse;
import com.eod.eod.domain.auth.presentation.dto.response.MobileTokenResponse;
import com.eod.eod.domain.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Base64;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MobileAuthController {

    private final BsmOAuthService bsmOAuthService;
    private final MobileOAuthStateService mobileOAuthStateService;
    private final MobileAuthTokenService mobileAuthTokenService;
    private final AuthService authService;
    private final SecureRandom secureRandom = new SecureRandom();

    @GetMapping("/mobile/bsm/authorize")
    public ResponseEntity<BsmAuthorizeUrlResponse> authorizeMobileBsm(
            @RequestParam("redirectUri") String redirectUri,
            @RequestParam(value = "state", required = false) String requestedState
    ) {
        String state = requestedState == null || requestedState.isBlank() ? generateState() : requestedState;
        mobileOAuthStateService.save(state, redirectUri);
        return ResponseEntity.ok(new BsmAuthorizeUrlResponse(bsmOAuthService.buildAuthorizeUrl(state)));
    }

    @PostMapping("/mobile/exchange")
    public ResponseEntity<MobileTokenResponse> exchangeMobileToken(
            @Valid @RequestBody MobileTokenExchangeRequest request
    ) {
        MobileAuthToken token = mobileAuthTokenService.consume(request.oneTimeToken());
        return ResponseEntity.ok(MobileTokenResponse.of(
                token.getAccessToken(),
                token.getRefreshToken(),
                token.getUser()
        ));
    }

    @PostMapping("/mobile/refresh")
    public ResponseEntity<MobileTokenResponse> refreshMobileToken(
            @Valid @RequestBody MobileRefreshRequest request
    ) {
        AuthService.RefreshTokenResult result = authService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(MobileTokenResponse.of(
                result.getAccessToken(),
                result.getRefreshToken(),
                result.getUser()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> me(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(AuthUserResponse.from(currentUser));
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
