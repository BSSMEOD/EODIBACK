package com.eod.eod.domain.item.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "소유권 주장 요청")
public class ItemClaimRequest {

    @NotNull(message = "학생 ID는 필수입니다.")
    @JsonProperty("student_id")
    @Schema(description = "소유권을 주장하는 학생 ID", example = "1")
    private Long studentId;

    @NotBlank(message = "주장 이유는 필수입니다.")
    @JsonProperty("claim_reason")
    @Schema(description = "본인이 소유자임을 주장하는 이유", example = "제 이어폰이고, 3층에서 분실했습니다.")
    private String claimReason;
}