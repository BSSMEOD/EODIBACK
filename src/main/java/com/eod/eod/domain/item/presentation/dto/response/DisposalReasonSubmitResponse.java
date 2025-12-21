package com.eod.eod.domain.item.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DisposalReasonSubmitResponse {

    @Schema(description = "응답 메시지", example = "보류 사유가 성공적으로 제출되었습니다.")
    private String message;

    public static DisposalReasonSubmitResponse of(String message) {
        return DisposalReasonSubmitResponse.builder()
                .message(message)
                .build();
    }
}
