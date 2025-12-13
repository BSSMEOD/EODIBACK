package com.eod.eod.domain.item.presentation.dto;

import com.eod.eod.domain.item.model.DisposalReason;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "폐기 보류 사유 조회 응답")
public class DisposalReasonResponse {

    @JsonProperty("item_id")
    @Schema(description = "물품 ID", example = "1")
    private Long itemId;

    @Schema(description = "물품 이미지 URL", example = "www.notion.so")
    private String image;

    @JsonProperty("teacher_name")
    @Schema(description = "선생님 이름", example = "육은찬")
    private String teacherName;

    @Schema(description = "보류 사유", example = "학생이 찾을 가능성이 있어 보류합니다.")
    private String reason;

    @JsonProperty("extension_days")
    @Schema(description = "폐기 기간 연장 일수", example = "5")
    private Integer extensionDays;

    public static DisposalReasonResponse from(DisposalReason disposalReason) {
        return DisposalReasonResponse.builder()
                .itemId(disposalReason.getItem().getId())
                .image(disposalReason.getItem().getImage())
                .teacherName(disposalReason.getTeacher().getName())
                .reason(disposalReason.getReason())
                .extensionDays(disposalReason.getExtensionDays())
                .build();
    }
}
