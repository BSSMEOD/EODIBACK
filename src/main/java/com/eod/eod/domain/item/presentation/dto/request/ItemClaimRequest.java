package com.eod.eod.domain.item.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemClaimRequest {

    @NotBlank(message = "주장 이유는 필수입니다.")
    @JsonProperty("claimReason")
    @Schema(description = "본인이 소유자임을 주장하는 이유", example = "제 이어폰이고, 3층에서 분실했습니다.")
    private String claimReason;
}
