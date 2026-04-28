package com.eod.eod.domain.discord.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record PickupDateRequest(
        @NotBlank(message = "discordId는 필수입니다.")
        String discordId,
        @NotBlank(message = "pickupDate는 필수입니다.")
        String pickupDate
) {}
