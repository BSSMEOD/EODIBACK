package com.eod.eod.domain.item.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ItemUpdateResponse {

    @Schema(description = "응답 메시지", example = "분실물이 성공적으로 수정되었습니다.")
    private final String message;

    public static ItemUpdateResponse success() {
        return new ItemUpdateResponse("분실물이 성공적으로 수정되었습니다.");
    }
}
