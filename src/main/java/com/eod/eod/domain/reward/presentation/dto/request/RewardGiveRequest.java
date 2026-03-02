package com.eod.eod.domain.reward.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RewardGiveRequest {

    @NotNull(message = "물품 ID는 필수입니다.")
    @JsonProperty("itemId")
    @Schema(description = "상점과 연결된 물품 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long itemId;
}
