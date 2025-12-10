package com.eod.eod.domain.reward.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "상점 지급 여부 조회 응답")
public class RewardEligibleResponse {

    @JsonProperty("studentId")
    @Schema(description = "학생 ID", example = "1")
    private Long studentId;

    @JsonProperty("itemId")
    @Schema(description = "조회 대상 물품 ID", example = "1")
    private Long itemId;

    @JsonProperty("id")
    @Schema(description = "상점 지급 기록 ID (null이면 미지급 상태)", example = "1", nullable = true)
    private Long id;

    @JsonProperty("createdAt")
    @Schema(description = "상점 지급 일시 (null이면 미지급 상태)", example = "2025-07-31T00:00:00", nullable = true)
    private LocalDateTime createdAt;
}
