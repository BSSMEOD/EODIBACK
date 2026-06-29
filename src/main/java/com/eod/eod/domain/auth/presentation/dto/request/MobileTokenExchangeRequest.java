package com.eod.eod.domain.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MobileTokenExchangeRequest(
        @NotBlank(message = "oneTimeToken은 필수입니다.")
        String oneTimeToken
) {
}
