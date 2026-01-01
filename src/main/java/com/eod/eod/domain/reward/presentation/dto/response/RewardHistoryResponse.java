package com.eod.eod.domain.reward.presentation.dto.response;

import com.eod.eod.domain.reward.model.RewardRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class RewardHistoryResponse {

    @JsonProperty("userId")
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

    /**
     * 검색 결과를 RewardHistoryResponse로 변환
     * userId는 검색 조건으로 사용된 값이며, null일 수 있습니다.
     */
    public static RewardHistoryResponse fromRecords(List<RewardRecord> records, Long userId) {
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
    public static class RewardInfo {

        @JsonProperty("rewardId")
        @Schema(description = "상점 지급 기록 ID", example = "12")
        private Long rewardId;

        @JsonProperty("itemId")
        @Schema(description = "지급과 연결된 물품 ID", example = "5")
        private Long itemId;

        @JsonProperty("itemName")
        @Schema(description = "지급 물품 이름", example = "무선 이어폰")
        private String itemName;

        @JsonProperty("item")
        @Schema(description = "물품 상세 정보")
        private ItemInfo item;

        @JsonProperty("givenBy")
        @Schema(description = "상점을 지급한 교사 이름", example = "김선생")
        private String givenBy;

        @JsonProperty("givenAt")
        @Schema(description = "상점 지급 날짜", example = "2025-07-31")
        private String givenAt;

        public static RewardInfo from(RewardRecord record) {
            return RewardInfo.builder()
                    .rewardId(record.getId())
                    .itemId(record.getItem().getId())
                    .itemName(record.getItem().getName())
                    .item(ItemInfo.from(record.getItem()))
                    .givenBy(record.getTeacher().getName())
                    .givenAt(formatDate(record.getCreatedAt()))
                    .build();
        }

        private static String formatDate(LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    @Getter
    @Builder
    public static class ItemInfo {

        @Schema(description = "물품 ID", example = "10")
        private Long id;

        @Schema(description = "물품 이름", example = "USB")
        private String name;

        @Schema(description = "물품 카테고리", example = "전자기기")
        private com.eod.eod.domain.item.model.Item.ItemCategory category;

        @Schema(description = "물품 상태", example = "GIVEN")
        private String status;

        @JsonProperty("imageUrl")
        @Schema(description = "물품 이미지 URL")
        private String imageUrl;

        @JsonProperty("foundAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        @Schema(description = "습득 날짜", example = "2025-12-01 10:00")
        private LocalDateTime foundAt;

        public static ItemInfo from(com.eod.eod.domain.item.model.Item item) {
            return ItemInfo.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .category(item.getCategory())
                    .status(item.getStatus().name())
                    .imageUrl(item.getImage())
                    .foundAt(item.getFoundAt())
                    .build();
        }
    }
}
