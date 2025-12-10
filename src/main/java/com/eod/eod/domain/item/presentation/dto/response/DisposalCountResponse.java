package com.eod.eod.domain.item.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
    description = "폐기 예정 물품 개수 응답",
    example = "{\"count\": 12}"
)
public class DisposalCountResponse {

    @Schema(description = "폐기 예정 물품 개수", example = "12")
    private long count;

    public static DisposalCountResponse of(long count) {
        return new DisposalCountResponse(count);
    }
}
