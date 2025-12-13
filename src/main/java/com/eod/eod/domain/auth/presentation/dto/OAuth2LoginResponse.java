package com.eod.eod.domain.auth.presentation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2LoginResponse {
    private String accessToken;
    private String tokenType;
    private UserInfo user;
    private String message;

    @Getter
    @Builder
    public static class UserInfo {
        private Long userId;
        private String email;
        private String name;
    }
}
