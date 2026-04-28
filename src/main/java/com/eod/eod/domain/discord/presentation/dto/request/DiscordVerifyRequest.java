package com.eod.eod.domain.discord.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DiscordVerifyRequest(
        String studentId,

        @NotBlank(message = "nickname은 필수입니다.")
        String nickname,

        @NotBlank(message = "discordUserId는 필수입니다.")
        String discordUserId
) {
}
