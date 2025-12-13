package com.eod.eod.domain.auth.presentation.dto;

import lombok.Getter;

@Getter
public class TokenResponse {
    private String accessToken;
    private String tokenType;

    public static TokenResponse of(String accessToken, String tokenType){
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.accessToken = accessToken;
        tokenResponse.tokenType = tokenType;
        return tokenResponse;
    }

}
