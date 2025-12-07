package com.eod.eod.domain.reward.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(
    description = "상점 지급 응답",
    example = "{\"message\": \"상점이 성공적으로 지급되었습니다.\"}"
)
public class RewardGiveResponse {

    @Schema(description = "응답 메시지", example = "상점이 성공적으로 지급되었습니다.")
    private final String message;

    public static RewardGiveResponse success() {
        return new RewardGiveResponse("상점이 성공적으로 지급되었습니다.");
    }
}