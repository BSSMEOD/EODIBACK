package com.eod.eod.domain.auth.presentation.dto.request;

import com.eod.eod.domain.user.model.User;
import jakarta.validation.constraints.NotNull;

public record DevLoginRequest(
        @NotNull(message = "role은 필수입니다.")
        User.Role role
) {
}
