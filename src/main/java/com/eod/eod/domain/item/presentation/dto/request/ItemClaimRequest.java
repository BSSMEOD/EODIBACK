package com.eod.eod.domain.item.presentation.dto.request;

import com.eod.eod.common.validation.ValidVisitDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ItemClaimRequest {

    @NotBlank(message = "주장 이유는 필수입니다.")
    @JsonProperty("claimReason")
    @Schema(description = "본인이 소유자임을 주장하는 이유", example = "제 이어폰이고, 3층에서 분실했습니다.")
    private String claimReason;

    @NotNull(message = "방문 날짜는 필수입니다.")
    @Future(message = "방문 날짜는 오늘 이후여야 합니다.")
    @ValidVisitDate
    @JsonProperty("visitDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "찾으러 올 날짜 (평일만 가능, 시간은 13:10 고정)", example = "2025-03-12")
    private LocalDate visitDate;
}
