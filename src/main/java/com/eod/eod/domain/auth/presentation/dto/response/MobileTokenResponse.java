package com.eod.eod.domain.auth.presentation.dto.response;

import com.eod.eod.domain.user.model.User;

public record MobileTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        AuthUserResponse user
) {
    public static MobileTokenResponse of(String accessToken, String refreshToken, User user) {
        return new MobileTokenResponse(accessToken, refreshToken, "Bearer", AuthUserResponse.from(user));
    }
}
