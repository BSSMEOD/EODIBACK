package com.eod.eod.domain.item.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(
    description = "소유권 주장 응답",
    example = "{\"message\": \"소유권 주장이 정상적으로 등록되었습니다.\"}"
)
public class ItemClaimResponse {

    @JsonProperty("message")
    @Schema(description = "응답 메시지", example = "소유권 주장이 정상적으로 등록되었습니다.")
    private String message;

    public static ItemClaimResponse success() {
        return ItemClaimResponse.builder()
                .message("소유권 주장이 정상적으로 등록되었습니다.")
                .build();
    }
}
