package com.eod.eod.domain.reward.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RewardEligibleCountResponse {

    @Schema(description = "상점 지급 대기 건수", example = "3")
    private long count;

    public static RewardEligibleCountResponse of(long count) {
        return new RewardEligibleCountResponse(count);
    }
}
