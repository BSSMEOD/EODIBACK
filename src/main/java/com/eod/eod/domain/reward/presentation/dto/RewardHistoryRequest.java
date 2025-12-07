package com.eod.eod.domain.reward.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "상점 지급 이력 조회 요청")
public class RewardHistoryRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    @JsonProperty("user_id")
    @Schema(description = "조회할 사용자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
}
