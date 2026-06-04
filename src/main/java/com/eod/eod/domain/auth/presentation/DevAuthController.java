package com.eod.eod.domain.auth.presentation;

import com.eod.eod.domain.auth.application.AuthService;
import com.eod.eod.domain.auth.application.DevLoginService;
import com.eod.eod.domain.auth.presentation.dto.request.DevLoginRequest;
import com.eod.eod.domain.auth.presentation.dto.response.MobileTokenResponse;
import com.eod.eod.domain.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class DevAuthController {

    private final DevLoginService devLoginService;

    @PostMapping("/dev-login")
    public ResponseEntity<MobileTokenResponse> devLogin(@Valid @RequestBody DevLoginRequest request) {
        AuthService.TokenPair tokenPair = devLoginService.login(request.role());
        User user = devLoginService.getDevUser(request.role());
        return ResponseEntity.ok(MobileTokenResponse.of(
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken(),
                user
        ));
    }
}
