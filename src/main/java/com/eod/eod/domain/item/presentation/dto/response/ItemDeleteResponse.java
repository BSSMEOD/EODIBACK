package com.eod.eod.domain.item.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
    description = "물품 삭제 응답",
    example = "{\"message\": \"분실물이 성공적으로 삭제되었습니다.\"}"
)
public class ItemDeleteResponse {

    private String message;

    public static ItemDeleteResponse success() {
        return new ItemDeleteResponse("분실물이 성공적으로 삭제되었습니다.");
    }
}
