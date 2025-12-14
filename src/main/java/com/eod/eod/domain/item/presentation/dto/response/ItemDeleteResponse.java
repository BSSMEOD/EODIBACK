package com.eod.eod.domain.item.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemDeleteResponse {

    @Schema(description = "응답 메시지", example = "분실물이 성공적으로 삭제되었습니다.")
    private String message;

    public static ItemDeleteResponse success() {
        return new ItemDeleteResponse("분실물이 성공적으로 삭제되었습니다.");
    }
}
