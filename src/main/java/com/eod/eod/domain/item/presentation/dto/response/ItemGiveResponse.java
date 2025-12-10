package com.eod.eod.domain.item.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(
    description = "물품 지급 응답",
    example = "{\"message\": \"물품 지급이 완료되었습니다.\"}"
)
public class ItemGiveResponse {

    @Schema(description = "응답 메시지", example = "물품 지급이 완료되었습니다.")
    private final String message;

    public static ItemGiveResponse success() {
        return new ItemGiveResponse("물품 지급이 완료되었습니다.");
    }
}
