package com.eod.eod.domain.reward.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(
        description = "날짜/학년/반으로 조회한 상점 지급 이력 응답",
        example = """
                {
                  "histories": [
                    {
                      "received_at": "2025-08-05",
                      "student_name": "홍길동",
                      "item_name": "이어폰",
                      "given_at": "2025-08-05"
                    }
                  ]
                }
                """
)
public class RewardGiveHistoryResponse {

    @JsonProperty("histories")
    @Schema(description = "상점 지급 이력 목록")
    private List<RewardGiveHistoryItem> histories;

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "상점 지급 이력 단건")
    public static class RewardGiveHistoryItem {

        @JsonProperty("received_at")
        @Schema(description = "학생이 상점을 수령한 날짜", example = "2025-08-05")
        private String receivedAt;

        @JsonProperty("student_name")
        @Schema(description = "학생 이름", example = "홍길동")
        private String studentName;

        @JsonProperty("item_name")
        @Schema(description = "지급된 물품 이름", example = "이어폰")
        private String itemName;

        @JsonProperty("given_at")
        @Schema(description = "상점을 지급한 날짜", example = "2025-08-05")
        private String givenAt;
    }
}
