package com.eod.eod.domain.reward.presentation.dto;

import com.eod.eod.domain.reward.model.RewardRecord;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "상점 지급 이력 응답")
public class RewardHistoryResponse {

    @JsonProperty("user_id")
    @Schema(description = "이력을 조회한 사용자 ID", example = "1")
    private Long userId;

    @JsonProperty("rewards")
    @Schema(description = "상점 지급 이력 목록")
    private List<RewardInfo> rewards;

    public static RewardHistoryResponse from(Long userId, List<RewardRecord> records) {
        List<RewardInfo> rewards = records.stream()
                .map(RewardInfo::from)
                .collect(Collectors.toList());

        return RewardHistoryResponse.builder()
                .userId(userId)
                .rewards(rewards)
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "상점 지급 상세 정보")
    public static class RewardInfo {

        @JsonProperty("reward_id")
        @Schema(description = "상점 지급 기록 ID", example = "12")
        private Long rewardId;

        @JsonProperty("item_id")
        @Schema(description = "지급과 연결된 물품 ID", example = "5")
        private Long itemId;

        @JsonProperty("item_name")
        @Schema(description = "지급 물품 이름", example = "무선 이어폰")
        private String itemName;

        @JsonProperty("given_by")
        @Schema(description = "상점을 지급한 교사 이름", example = "김선생")
        private String givenBy;

        @JsonProperty("given_at")
        @Schema(description = "상점 지급 날짜", example = "2025-07-31")
        private String givenAt;

        public static RewardInfo from(RewardRecord record) {
            return RewardInfo.builder()
                    .rewardId(record.getId())
                    .itemId(record.getItem().getId())
                    .itemName(record.getItem().getName())
                    .givenBy(record.getTeacher().getName())
                    .givenAt(formatDate(record.getCreatedAt()))
                    .build();
        }

        private static String formatDate(LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}