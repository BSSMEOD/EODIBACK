package com.eod.eod.domain.item.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
    description = "분실물 등록 응답",
    example = "{\"item_id\": 104, \"message\": \"분실물이 성공적으로 등록되었습니다.\"}"
)
public class ItemCreateResponse {

    @JsonProperty("itemId")
    @Schema(description = "등록된 물품 ID", example = "104")
    private Long itemId;

    @Schema(description = "결과 메시지", example = "분실물이 성공적으로 등록되었습니다.")
    private String message;

    public static ItemCreateResponse success(Long itemId) {
        return new ItemCreateResponse(itemId, "분실물이 성공적으로 등록되었습니다.");
    }
}
