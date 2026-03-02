package com.eod.eod.domain.reward.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RewardEligibleResponse {

    @JsonProperty("studentId")
    @Schema(description = "습득 신고자 학생 ID", example = "1")
    private Long studentId;

    @JsonProperty("studentName")
    @Schema(description = "습득 신고자 이름", example = "홍길동")
    private String studentName;

    @JsonProperty("studentCode")
    @Schema(description = "습득 신고자 학번 (학년*1000 + 반*100 + 번호)", example = "1101", nullable = true)
    private Integer studentCode;

    @JsonProperty("itemId")
    @Schema(description = "조회 대상 물품 ID", example = "1")
    private Long itemId;

    @JsonProperty("rewardGiven")
    @Schema(description = "상점 지급 완료 여부", example = "false")
    private boolean rewardGiven;

    @JsonProperty("givenAt")
    @Schema(description = "상점 지급 일시 (null이면 미지급 상태)", example = "2025-07-31T00:00:00", nullable = true)
    private LocalDateTime givenAt;
}
