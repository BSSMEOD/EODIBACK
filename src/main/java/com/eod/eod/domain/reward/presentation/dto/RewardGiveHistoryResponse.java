package com.eod.eod.domain.reward.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RewardGiveHistoryResponse {

    @JsonProperty("histories")
    private List<RewardGiveHistoryItem> histories;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RewardGiveHistoryItem {

        @JsonProperty("received_at")
        private String receivedAt;

        @JsonProperty("student_name")
        private String studentName;

        @JsonProperty("item_name")
        private String itemName;

        @JsonProperty("given_at")
        private String givenAt;
    }
}