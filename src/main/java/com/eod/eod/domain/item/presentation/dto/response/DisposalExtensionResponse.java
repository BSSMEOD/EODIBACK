package com.eod.eod.domain.item.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class DisposalExtensionResponse {

    @Schema(description = "응답 메시지", example = "폐기 보류 되었습니다.")
    private String message;

    @Schema(description = "연장된 폐기 예정일", example = "2024-12-31")
    private String extendedDisposalDate;

    public static DisposalExtensionResponse of(String message, String extendedDisposalDate) {
        return DisposalExtensionResponse.builder()
                .message(message)
                .extendedDisposalDate(extendedDisposalDate)
                .build();
    }
}
