package com.eod.eod.domain.item.presentation.dto.request;

import com.eod.eod.common.validation.Weekday;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ItemClaimRequest {

    @NotNull(message = "방문 날짜는 필수입니다.")
    @Weekday
    @JsonProperty("visitDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "찾으러 올 날짜 (평일만 가능, 시간은 13:10 고정)", example = "2025-03-12")
    private LocalDate visitDate;
}
