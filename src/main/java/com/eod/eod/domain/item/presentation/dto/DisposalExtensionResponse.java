package com.eod.eod.domain.item.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "폐기 기간 연장 응답")
public class DisposalExtensionResponse {

    @Schema(description = "응답 메시지", example = "페기 보류 되었습니다.")
    private String message;

    public static DisposalExtensionResponse of(String message) {
        return DisposalExtensionResponse.builder()
                .message(message)
                .build();
    }
}
