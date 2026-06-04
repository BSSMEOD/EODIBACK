package com.eod.eod.domain.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MobileRefreshRequest(
        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}
