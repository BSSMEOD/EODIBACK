package com.eod.eod.domain.item.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "폐기 기간 연장 요청")
public class DisposalExtensionRequest {

    @NotNull(message = "보류 사유 ID는 필수입니다.")
    @JsonProperty("reasonId")
    @Schema(description = "보류 사유 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reasonId;
}
