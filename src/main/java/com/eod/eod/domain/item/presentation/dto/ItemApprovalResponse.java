package com.eod.eod.domain.item.presentation.dto;

import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.user.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@Schema(description = "물품 승인 응답")
public class ItemApprovalResponse {

    @JsonProperty("item_id")
    @Schema(description = "승인/거절된 분실물 ID", example = "1")
    private Long itemId;

    @JsonProperty("approval_status")
    @Schema(description = "최종 처리 상태 (APPROVED 또는 REJECTED)", example = "APPROVED")
    private String approvalStatus;

    @JsonProperty("approver")
    @Schema(description = "승인 처리한 관리자 정보")
    private ApproverResponse approver;

    @JsonProperty("approved_at")
    @Schema(description = "처리 시각", example = "2025-08-02")
    private String approvedAt;

    @JsonProperty("message")
    @Schema(description = "처리 결과 메시지", example = "소유권이 승인되었습니다.")
    private String message;

    public static ItemApprovalResponse from(Item item) {
        String message = item.getApprovalStatus() == Item.ApprovalStatus.APPROVED
                ? "소유권이 승인되었습니다."
                : "소유권이 거절되었습니다.";

        return ItemApprovalResponse.builder()
                .itemId(item.getId())
                .approvalStatus(item.getApprovalStatus().name())
                .approver(ApproverResponse.from(item.getApprovedBy()))
                .approvedAt(formatDate(item.getApprovedAt()))
                .message(message)
                .build();
    }

    private static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Getter
    @Builder
    @Schema(description = "승인 처리자 정보")
    public static class ApproverResponse {

        @JsonProperty("id")
        @Schema(description = "관리자 ID", example = "12")
        private Long id;

        @JsonProperty("name")
        @Schema(description = "관리자 이름", example = "이하은")
        private String name;

        public static ApproverResponse from(User user) {
            return ApproverResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .build();
        }
    }
}