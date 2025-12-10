package com.eod.eod.domain.item.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "폐기 보류 사유 제출 요청")
public class DisposalReasonRequest {

    @NotBlank(message = "보류 사유는 필수입니다.")
    @Schema(description = "폐기 보류 사유", example = "학생이 찾을 가능성이 있어 보류합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @NotNull(message = "연장 일수는 필수입니다.")
    @Positive(message = "연장 일수는 양수여야 합니다.")
    @Max(value = 365, message = "연장 일수는 365일을 초과할 수 없습니다.")
    @Schema(description = "폐기 기간 연장 일수", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer days;
}
