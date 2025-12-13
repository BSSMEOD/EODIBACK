package com.eod.eod.domain.reward.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "상점 지급 요청")
public class RewardGiveRequest {

    @NotNull(message = "학생 ID는 필수입니다.")
    @JsonProperty("student_id")
    @Schema(description = "상점을 받을 학생 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long studentId;

    @NotNull(message = "물품 ID는 필수입니다.")
    @JsonProperty("item_id")
    @Schema(description = "상점과 연결된 물품 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long itemId;
}