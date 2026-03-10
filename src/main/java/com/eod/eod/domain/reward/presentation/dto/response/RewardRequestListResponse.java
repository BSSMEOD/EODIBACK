package com.eod.eod.domain.reward.presentation.dto.response;

import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.reward.model.RewardRecord;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class RewardRequestListResponse {

    @JsonProperty("rewards")
    @Schema(description = "상점 지급 리스트")
    private List<RewardRequestItem> rewards;

    private RewardRequestListResponse(List<RewardRequestItem> rewards) {
        this.rewards = rewards;
    }

    public static RewardRequestListResponse from(List<Item> items, Map<Long, RewardRecord> rewardMap) {
        return new RewardRequestListResponse(
                items.stream()
                        .map(item -> RewardRequestItem.from(item, rewardMap.get(item.getId())))
                        .collect(Collectors.toList())
        );
    }

    @Getter
    public static class RewardRequestItem {

        @JsonProperty("itemId")
        @Schema(description = "물품 ID", example = "1")
        private Long itemId;

        @JsonProperty("itemName")
        @Schema(description = "물품명", example = "무선 이어폰")
        private String itemName;

        @JsonProperty("imageUrl")
        @Schema(description = "물품 이미지 URL", example = "https://example.com/image.jpg")
        private String imageUrl;

        @JsonProperty("reporterName")
        @Schema(description = "분실물 신고자 이름", example = "홍길동")
        private String reporterName;

        @JsonProperty("reporterStudentCode")
        @Schema(description = "신고자 학번", example = "2024001", nullable = true)
        private Integer reporterStudentCode;

        @JsonProperty("claimedAt")
        @Schema(description = "주인이 찾아간 일시", example = "2025-02-13T12:34:56")
        private LocalDateTime claimedAt;

        @JsonProperty("isRewarded")
        @Schema(description = "상점 지급 여부", example = "false")
        private boolean isRewarded;

        private RewardRequestItem(Long itemId, String itemName, String imageUrl, String reporterName,
                                  Integer reporterStudentCode, LocalDateTime claimedAt, boolean isRewarded) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.imageUrl = imageUrl;
            this.reporterName = reporterName;
            this.reporterStudentCode = reporterStudentCode;
            this.claimedAt = claimedAt;
            this.isRewarded = isRewarded;
        }

        public static RewardRequestItem from(Item item, RewardRecord record) {
            return new RewardRequestItem(
                    item.getId(),
                    item.getName(),
                    item.getImage(),
                    item.getStudent().getName(),
                    item.getStudent().getStudentCode(),
                    item.getApprovedAt(),
                    record != null
            );
        }
    }
}
