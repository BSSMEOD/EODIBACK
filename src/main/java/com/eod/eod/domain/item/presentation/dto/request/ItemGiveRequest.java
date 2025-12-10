package com.eod.eod.domain.item.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "물품 지급 요청")
public class ItemGiveRequest {

    @NotNull(message = "학생 ID는 필수입니다.")
    @JsonProperty("studentId")
    @Schema(description = "지급 받을 학생 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long studentId;
}
