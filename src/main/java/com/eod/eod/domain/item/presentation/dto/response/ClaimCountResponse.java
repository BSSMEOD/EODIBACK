package com.eod.eod.domain.item.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
    description = "회수 신청 건수 응답",
    example = "{\"count\": 8}"
)
public class ClaimCountResponse {

    @Schema(description = "회수 신청 건수", example = "8")
    private long count;

    public static ClaimCountResponse of(long count) {
        return new ClaimCountResponse(count);
    }
}
